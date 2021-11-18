package googledriveapi;
import java.io.IOException;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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
	public void preview() {
		
	
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
	public void delete(String path) {
		try {
			Drive service = getDriveService();
			 service.files().delete(path).execute();   
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
		} 
		
	}

	@Override
	public void transfer(String location, String destination, String DriveFile) {
		//String fileId = "1sTWaJ_j7PkjzaBWtNc3IzovK5hQf21FbOw9yLeeLPNQ";
		//String folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E";
		// Retrieve the existing parents to remove
		try {
			File file = getDriveService().files().get(DriveFile)
				    .setFields("parents")
				    .execute();
				StringBuilder previousParents = new StringBuilder();
				for (String parent : file.getParents()) {
				  previousParents.append(parent);
				  previousParents.append(',');
				}
				// Move the file to the new folder
				file = getDriveService().files().update(DriveFile, null)
				    .setAddParents(destination)
				    .setRemoveParents(previousParents.toString())
				    .setFields("id, parents")
				    .execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	@Override
	public void previewExt(String f, String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void previewDir(String path) {
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
	public void download(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createFolders(String path,String name, String number) {
		// TODO Auto-generated method stub
		
	}
}
