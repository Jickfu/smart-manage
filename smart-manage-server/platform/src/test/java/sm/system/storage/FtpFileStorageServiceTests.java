package sm.system.storage;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FtpFileStorageServiceTests {

	private final FileStorageConfigProvider configProvider = mock(FileStorageConfigProvider.class);
	private final FTPClient ftpClient = mock(FTPClient.class);
	private final FtpFileStorageService storage = new TestFtpFileStorageService(configProvider, ftpClient);

	@Test
	void loginFailureIsReportedAsStorageFailure() throws IOException {
		when(configProvider.getFileStorageConfig()).thenReturn(config());
		when(ftpClient.login("user", "password")).thenReturn(false);
		when(ftpClient.isConnected()).thenReturn(true);

		IOException exception = assertThrows(IOException.class, () -> storage.delete("temp/file.txt"));

		assertEquals("FTP 登录失败: null", exception.getMessage());
		verify(ftpClient).disconnect();
	}

	@Test
	void deleteFailureIsNotSilentlyIgnored() throws IOException {
		prepareConnectedClient();
		when(ftpClient.deleteFile("sys/file.txt")).thenReturn(false);
		when(ftpClient.listFiles("sys/file.txt")).thenReturn(new FTPFile[0]);
		when(ftpClient.getReplyCode()).thenReturn(550);

		assertThrows(IOException.class, () -> storage.delete("sys/file.txt"));
	}

	@Test
	void missingFileIsAcceptedForIdempotentCleanupRetry() throws IOException {
		prepareConnectedClient();
		when(ftpClient.deleteFile("sys/file.txt")).thenReturn(false);
		when(ftpClient.listFiles("sys/file.txt")).thenReturn(new FTPFile[0]);
		when(ftpClient.getReplyCode()).thenReturn(226);

		storage.delete("sys/file.txt");

		verify(ftpClient).listFiles("sys/file.txt");
	}

	private void prepareConnectedClient() throws IOException {
		when(configProvider.getFileStorageConfig()).thenReturn(config());
		when(ftpClient.login("user", "password")).thenReturn(true);
		when(ftpClient.setFileType(FTP.BINARY_FILE_TYPE)).thenReturn(true);
	}

	private FileStorageConfig config() {
		return new FileStorageConfig("FTP", null, "localhost", 21, "user", "password", null, false,
				null, null, null, null, null, null);
	}

	private static class TestFtpFileStorageService extends FtpFileStorageService {

		private final FTPClient ftpClient;

		private TestFtpFileStorageService(FileStorageConfigProvider configProvider, FTPClient ftpClient) {
			super(configProvider);
			this.ftpClient = ftpClient;
		}

		@Override
		protected FTPClient createClient() {
			return ftpClient;
		}
	}
}
