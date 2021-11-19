package googledriveapi;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import storage.*;


public class GoogleDriveImpl extends Storage{
	
	static {
		StorageManager.registerStorage(new GoogleDriveImpl());
	}
	
	public GoogleDriveImpl () {
		
	}
	/**
	 * Application name.
	 */
	private static final String APPLICATION_NAME = "My project";

	/**
	 * Global instance of the {@link FileDataStoreFactory}.
	 */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/**
	 * Global instance of the JSON factory.
	 */
	private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/**
	 * Global instance of the HTTP transport.
	 */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 * <p>
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/calendar-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = GoogleDriveImpl.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
			clientSecrets, SCOPES).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		return credential;
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 */
	public static Drive getDriveService() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
			.setApplicationName(APPLICATION_NAME)
			.build();
	}


	@Override
	public void previewAll(String path) {
		try {
			Drive service = getDriveService();

			FileList result = service.files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
			List<File> files = result.getFiles();
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
				System.out.println("Files:");
				for (File file : files) {
					
					System.out.printf("%s (%s)\n", file.getName(), file.getId());
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void createFiles(String path, String name, int maxFolders) {
		for(int i=0; i<maxFolders;i++) {
	//	path = "1a0b1RkeQnxh5tL9wA7VGfI7eHCZd2olR";
		File fileMetadata = new File();
		fileMetadata.setName(name);
		fileMetadata.setParents(Collections.singletonList(path));
		java.io.File filePath = new java.io.File(name);
		FileContent mediaContent = new FileContent("file"+i, filePath);
		File file;
		try {
			file = getDriveService().files().create(fileMetadata, mediaContent)
			    .setFields("id, parents")
			    .execute();
			System.out.println("File ID: " + file.getId());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
	}
	
	@Override
	public void delete(String path) {
		try {
			Drive service = getDriveService();
			 service.files().delete(path).execute();   
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
		} 
		
	}

	@Override
	public void transfer(String fileId, String folderId, String DriveFile) {
		//String fileId = "1sTWaJ_j7PkjzaBWtNc3IzovK5hQf21FbOw9yLeeLPNQ";
		//String folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E";
		// Retrieve the existing parents to remove
		try {
			File file = getDriveService().files().get(fileId)
				    .setFields("parents")
				    .execute();
				StringBuilder previousParents = new StringBuilder();
				for (String parent : file.getParents()) {
				  previousParents.append(parent);
				  previousParents.append(',');
				}
				// Move the file to the new folder
				file = getDriveService().files().update(fileId, null)
				    .setAddParents(folderId)
				    .setRemoveParents(previousParents.toString())
				    .setFields("id, parents")
				    .execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	@Override
	public void previewExt(String f, String s) {
		try {
			Drive service = getDriveService();

			FileList result = service.files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
			//FileList result = service.files().get(f)
			List<File> files = result.getFiles();
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
				System.out.println("Files:");
				for (File file : files) {
					if(file.getName().toString().contains(s))
					System.out.printf("%s (%s)\n", file.getName(), file.getId());
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void previewDir(String path) {
		try {
			Drive service = getDriveService();
			FileList result = service.files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
			File fl=service.files().get(path).execute();
			
			List<File> files = fl;
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
					System.out.println("Folders: ");
					for (File folder : files) {
						if(folder.getMimeType().contentEquals("application/vnd.google-apps.folder"))
							System.out.printf("%s (%s)\n", folder.getName(), folder.getId());	
				}
			
				System.out.println("Files:");
				for (File file : files) {
					System.out.printf("%s (%s)\n", file.getName(), file.getId());
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void download(String file) {
		java.io.File f = new java.io.File("C:\\Users\\38160\\git\\GoogleDriveImpl\\google-api\\down\\b");
		FileOutputStream in = null;
		try {
		    in = new FileOutputStream(f); 
		    
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    Drive service = getDriveService();
			service.files().get(file).executeMediaAndDownloadTo(out);;
		    // Put data in your out
		    out.writeTo(in);
		} catch(IOException ioe) {
		    // Handle exception here
		    ioe.printStackTrace();
		} finally {
		   // out.close();
		}
	}

	@Override
	public void createFolders(String path,String name, String number) {
		File fileMetadata = new File();
		fileMetadata.setName(name);
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		try {
			File file = getDriveService().files().create(fileMetadata)
				    .setFields("id")
				    .execute();
				System.out.println("Folder ID: " + file.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
<<<<<<< Upstream, based on origin/master
	public void initialise(User user, String storagePath) {
		// TODO Auto-generated method stub
		
=======
	public void previewSorted(String path) {
		try {
			Drive service = getDriveService();

			FileList result = service.files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
			List<File> files = result.getFiles();
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
				System.out.println("Files:");
				for (File file : files) {
					
					System.out.printf("%s (%s)\n", file.getName(), file.getId());
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void prevName(String path, String name) {
		try {
			Drive service = getDriveService();

			FileList result = service.files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
			//FileList result = service.files().get(f)
			int c=0;
			List<File> files = result.getFiles();
			Collections.sort(files);
			
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
				System.out.println("Files:");
				for (File file : files) {
					if(file.getName().contentEquals(name))
					System.out.printf("%s (%s)\n", file.getName(), file.getId());
					c++;
				}
				if(c==0) {
					System.out.print("Fajl sa tim imenom ne postoji!");
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void initialise(User user, String storagePath) {
		if(storagePath.contentEquals("https://drive.google.com/drive/u/2/my-drive")) {
		System.out.println("Korisnik" + user.getUsername() + "  pokusava da kreira skladiste...");
    	int flag=0;
    	if(user.getPrivileges().get(Permissions.create)) {
    		 flag++; 
    	}
    	if(flag>0) {
    		File fileMetadata = new File();
			fileMetadata.setName("Storage");
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			try {
			File file = getDriveService().files().create(fileMetadata)
				    	.setFields("id")
				    	.execute();
					System.out.println("Skladiste kreirano!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		
    	}else {
    		System.out.print("Korisnik nema privilegije za kreiranje skladista!");
    	}
		}else {
		System.out.print("Pogresan URL!");
		}
>>>>>>> f8b330d 7777
	}
}
