/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thesmartenergy.sparql.generate.api;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.github.thesmartenergy.sparql.generate.jena.engine.PlanFactory;
import com.github.thesmartenergy.sparql.generate.jena.engine.RootPlan;
import com.github.thesmartenergy.sparql.generate.jena.query.SPARQLGenerateQuery;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.Locator;

/**
 *
 * @author maxime.lefrancois
 */
@Path("")
public class Api extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(Api.class.getSimpleName());

    @GET
    @Path("/transform")
    public Response doGet(
            @Context Request r,
            @Context HttpServletRequest request,
            @DefaultValue("") @QueryParam("query") String query,
            @DefaultValue("http://localhost:8080/sparql-generate/query/example1.rqg") @QueryParam("queryuri") String queryuri,
            @DefaultValue("") @QueryParam("message") String message,
            @DefaultValue("message") @QueryParam("var") String var,
            @DefaultValue("") @QueryParam("accept") String accept)  throws IOException {

//        try {
            HttpSession session = request.getSession();
            List<UniqueLocator> locators = (List<UniqueLocator>) session.getAttribute("locators");
            if (null == locators) {
                locators = new ArrayList<>();
                session.setAttribute("locators", locators);
            }

            FileManager fileManager = new FileManager();
            for (Locator loc : locators) {
                fileManager.addLocator(loc);
            }
            
            System.out.println(queryuri);

            if("".equals(query)) {
                URL obj = new URL(queryuri);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/sparql-generate");
                con.setInstanceFollowRedirects(true);
                System.out.println("GET to "+queryuri+" returned "+con.getResponseCode());
                InputStream in = con.getInputStream();
                query = IOUtils.toString(in);
            }
            
            System.out.println("got " + query);

            // parse the SPARQL-Generate query and create plan
            PlanFactory factory = new PlanFactory(fileManager);
            
            Syntax syntax = SPARQLGenerate.SYNTAX;
            SPARQLGenerateQuery q = (SPARQLGenerateQuery) QueryFactory.create(query, syntax);
            q.setBaseURI("http://example.org/");
            RootPlan plan = factory.create(q);
            
            // create the initial model
            Model model = ModelFactory.createDefaultModel();

            String uri = "http://www.w3.org/2001/XMLSchema#string";
            QuerySolutionMap initialBinding = new QuerySolutionMap();
            TypeMapper typeMapper = TypeMapper.getInstance();
            RDFDatatype dt = typeMapper.getSafeTypeByName(uri);
            Node arqLiteral = NodeFactory.createLiteral(message, dt);
            RDFNode jenaLiteral = model.asRDFNode(arqLiteral);
            initialBinding.add(var, jenaLiteral) ; 

            // execute the plan
            plan.exec(initialBinding, model);

            System.out.println(accept);
            if(!accept.equals("text/turtle") && !accept.equals("application/rdf+xml")) {
                List<Variant> vs = Variant.mediaTypes(new MediaType("application", "rdf+xml"), new MediaType("text", "turtle")).build();
                Variant v = r.selectVariant(vs);
                accept = v.getMediaType().toString();
            }

            System.out.println(accept);
            StringWriter sw = new StringWriter();
            ResponseBuilder res;
            if (accept.equals("application/rdf+xml")) {
                model.write(sw, "RDF/XML", "http://example.org/"); 
                res = Response.ok(sw.toString(), "application/rdf+xml");
                res.header("Content-Disposition", "filename= message.rdf;");
                return res.build();
            } else {
                model.write(sw, "TTL", "http://example.org/");
                res = Response.ok(sw.toString(), "text/turtle");
                res.header("Content-Disposition", "filename= message.ttl;");
                return res.build();
            }
//        } catch (Exception e) {
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            return Response.serverError().entity(sw.toString()).build();
//        }
    }

}
