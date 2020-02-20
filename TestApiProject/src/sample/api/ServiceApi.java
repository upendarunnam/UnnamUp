package sample.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebInitParam;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
import com.google.gson.Gson;




@Path("/")
public class ServiceApi {
	
	String uploadDirectory;
	Connection conn;
	@Context
    ServletContext servletContext;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	
	@GET 
	@Path("/users")
    @Produces(MediaType.APPLICATION_JSON) 
	public Response getUsers(@HeaderParam("TokenId") String sessionID){ 
		
		if(sessionID != null && !sessionID.isEmpty()) {
			
			
			
			try {
				
				BASE64Decoder base64Dec = new BASE64Decoder();
				byte[] bytest = base64Dec.decodeBuffer(sessionID);
				
				String sessionId = new String(bytest);
				ApiUtils utils= new ApiUtils();
				conn = utils.getDbConn(servletContext.getInitParameter("DriverName"),servletContext.getInitParameter("DBUrl"), servletContext.getInitParameter("UName"), servletContext.getInitParameter("PWord"));
				
				ResultSet rs = conn.createStatement().executeQuery("select LAST_ACTIVE_TIME from USER_DATA1 WHERE SEC_TOKEN='"+sessionId.trim()+"'");
				Date lstActTime = null;
				while(rs.next()) {
					lstActTime = sdf.parse(rs.getString("LAST_ACTIVE_TIME"));
				}
				if(lstActTime != null) {
					
					long diff = new Date().getTime() - lstActTime.getTime();
					long diffSeconds = diff / 1000 % 60;
					if(diffSeconds < 181) {
						
						conn.createStatement().executeUpdate("UPDATE USER_DATA1 SET LAST_ACTIVE_TIME='"+sdf.format(new Date())+"' WHERE SEC_TOKEN='"+sessionId.trim()+"'");
						rs = conn.createStatement().executeQuery("SELECT USER_NAME,PHONE_NO FROM USER_DATA1");
						StringBuilder returnSt = new StringBuilder();
						returnSt.append("{ \"users\" : [" );
						while(rs.next()) {
							returnSt.append("{\"USER_NAME\": \""+rs.getString("USER_NAME")+"\",\"phone\": \""+rs.getString("PHONE_NO")+"\"}");
							
						}
						returnSt.append("] }" );
						return Response.status(Response.Status.OK) .entity(returnSt.toString()).build();
					}else {
						return Response.status(Response.Status.OK) .entity("Invalide session").build();
					}
	
				}else {
					return Response.status(Response.Status.OK) .entity("Invalide session").build();
				}

			}catch (Exception e) {
				if(conn!=null)
					try {
						conn.close();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				e.printStackTrace();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR) .entity("").build();
			}
			
		}else {
			return Response.status(Response.Status.BAD_REQUEST) .entity("").build();
		}
	} 
	
	
	@PUT
	@Path("/users")
	@Consumes(MediaType.APPLICATION_JSON)
	
	public Response addUsers(String userData) {
		
		
		if(userData != null && !userData.isEmpty()) {
			
			Gson gson = new Gson();
			User user = gson.fromJson(userData, User.class);
						 
		               
			if(user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
				
				if(user.getPassword() != null && !user.getPassword().isEmpty()) {
					
					ApiUtils utils= new ApiUtils();
					try {
						conn = utils.getDbConn(servletContext.getInitParameter("DriverName"),servletContext.getInitParameter("DBUrl"), servletContext.getInitParameter("UName"), servletContext.getInitParameter("PWord"));
						String sql = "INSERT INTO USER_DATA1 (USER_NAME,PHONE_NO,PASSWORD,LAST_ACTIVE_TIME) values "
								    + "('"+user.getUsername().trim()+"','"+user.getPhone().trim()+"','"+user.getPassword()+",'"+sdf.format(new Date())+"')";
						boolean isRegistred = conn.createStatement().execute(sql);
						conn.close();
						if(isRegistred) {
							return Response.status(Response.Status.OK) .entity("Registered").build();
						}else {
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR) .entity("Not Registered").build();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						if(conn!=null)
							try {
								conn.close();
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						e.printStackTrace();
						return Response.status(Response.Status.EXPECTATION_FAILED) .entity("Not Registered").build();
						
					}
					
				}else {
					return Response.status(Response.Status.BAD_REQUEST) .entity("Not Registered").build();
				}
				
			}else {
				return Response.status(Response.Status.BAD_REQUEST) .entity("Not Registered").build();
			}
      
		}else {
			return Response.status(Response.Status.BAD_REQUEST) .entity("Not Registered").build();
		}

		
	}
	
	@POST
	@Path("/logIn")
	@Consumes(MediaType.APPLICATION_JSON)
	
	public Response loginUser(String userData) {
		
		if(userData != null && !userData.isEmpty()) {
			
			Gson gson = new Gson();
			User user = gson.fromJson(userData, User.class);
				if(user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
				
					if(user.getPassword() != null && !user.getPassword().isEmpty()) {
						
						ApiUtils utils= new ApiUtils();
						try {
							String uniqueID = UUID.randomUUID().toString();
							conn = utils.getDbConn(servletContext.getInitParameter("DriverName"),servletContext.getInitParameter("DBUrl"), servletContext.getInitParameter("UName"), servletContext.getInitParameter("PWord"));
							int rowsUpdated = conn.createStatement().executeUpdate("UPDATE USER_DATA1 SET SEC_TOKEN='"+uniqueID+"',LAST_ACTIVE_TIME='"+sdf.format(new Date())
									+"' WHERE USER_NAME='"+user.getUsername()+"' AND PASSWORD='"+user.getPassword()+"'");
							if(rowsUpdated == 1) {
								
								BASE64Encoder base64en = new BASE64Encoder();
								String st64 = base64en.encode(uniqueID.getBytes());
						
								return Response.status(Response.Status.OK) .entity("{\"TokenId\":\""+st64+"\"}").build();
								
							}else {
								return Response.status(Response.Status.EXPECTATION_FAILED) .entity("").build();
							}
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							if(conn!=null)
								try {
									conn.close();
								} catch (SQLException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							e.printStackTrace();
							return Response.status(Response.Status.EXPECTATION_FAILED) .entity("").build();
							
						}
						
						
					}else {
						return Response.status(Response.Status.BAD_REQUEST) .entity("").build();
					}
				}else {
					return Response.status(Response.Status.BAD_REQUEST) .entity("").build();
				}
			
		}else {
			
			return Response.status(Response.Status.BAD_REQUEST) .entity("").build();
			
		}
		
		
		
	}
	
	@POST
	@Path("/logout/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	
	public Response logOut(@PathParam("id") String userID) {
		ApiUtils utils= new ApiUtils();
		String uniqueID = UUID.randomUUID().toString();
		try {
			if(userID != null && !userID.isEmpty()) {
				conn = utils.getDbConn(servletContext.getInitParameter("DriverName"),servletContext.getInitParameter("DBUrl"), servletContext.getInitParameter("UName"), servletContext.getInitParameter("PWord"));
				conn.createStatement().execute("UPDATE USER_DATA1 SET SEC_TOKEN='' WHERE USER_NAME='"+userID+"'");
			}
			return Response.status(Response.Status.OK) .entity("").build();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return Response.status(Response.Status.EXPECTATION_FAILED) .entity("").build();
		}finally {
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
	}

}
