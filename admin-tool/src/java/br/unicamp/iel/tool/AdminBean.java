package br.unicamp.iel.tool;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import uk.org.ponder.stringutil.FilenameUtil;
import br.unicamp.iel.logic.ReadInWebAdminLogic;
import br.unicamp.iel.model.Activity;
import br.unicamp.iel.model.Course;
import br.unicamp.iel.model.Module;
import br.unicamp.iel.tool.commons.CourseComponents;
import br.unicamp.iel.tool.commons.Unzip;

public class AdminBean {
	@Setter
	private String title;
	@Setter
	private String language;
	@Setter
	private String description;

	@Setter
	private Map<String, CommonsMultipartFile> files;

	@Setter
	private ReadInWebAdminLogic logic;

	@Setter
	ServerConfigurationService configuration;

	public String createCourse() {
		if (title == null || language == null) {
			return CourseComponents.CREATE_FAIL;
		} else {
			try {
				Course course = new Course(title, language);
				if (description != null) {
					course.setDescription(description);
				}
				logic.saveCourse(course);
				setupCourse(course);
			} catch (Exception e) {
				e.printStackTrace();
				return CourseComponents.CREATE_FAIL;
			}
		}

		return CourseComponents.CREATED;
	}

	private void setupCourse(Course course) {
		Module m = new Module(course, 1, "");
		logic.saveModule(m);
		Activity a = new Activity(m, 1, "", "", "", "", 0, "", new Date());
		logic.saveActivity(a);
	}

	public String exerciseHandler() {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return CourseComponents.DATA_SAVING_FAIL;
		}

		if (files.isEmpty())
			return CourseComponents.DATA_EMPTY;
		String riwUpload = configuration.getString("readinweb.upload");

		Iterator<String> it = files.keySet().iterator();
		if (it.hasNext()) {
			CommonsMultipartFile file = files.get(it.next());

			// Get zip filename
			String randomName = getRandomString(md, file.getOriginalFilename());
			String filename = randomName + "."
					+ FilenameUtil.getExtension(file.getOriginalFilename());

			// Save it
			File path = new File(getMediaPath(riwUpload, file.getContentType()));
			path.mkdirs();
			File f = new File(path + File.separator + filename);

			FileItem fi = file.getFileItem();
			try {
				fi.write(f);
			} catch (Exception e) {
				e.printStackTrace();
				return CourseComponents.DATA_SAVING_FAIL;
			}

			// Now extract it to:
			// riwUpload/courselanguage/exercises/randomdir/
			File exercisePath = new File(riwUpload + File.separator + "english"
					+ File.separator + "exercises" + File.separator
					+ randomName);
			exercisePath.mkdirs();

			Unzip unzip = new Unzip();
			unzip.unZipIt(f, exercisePath);

			// TODO: Test manifest!!
			// If all ok, save to the database

		}
		return CourseComponents.DATA_SAVED;
	}

	public String handleContentSent() {
		// Get content Type and verify if the content is valid
		// Verify if the content is valid accordling with the type passed
		// like activity, exercise or stuff
		// if all ok, insert data on the database taking care with do not insert
		// repeated data, of course.
		// If it is exercise contents, save it on the correct place considering
		// The content should come with the correct structure.
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return CourseComponents.DATA_SAVING_FAIL;
		}

		if (files.isEmpty())
			return CourseComponents.DATA_EMPTY;
		String riwUpload = configuration.getString("readinweb.upload");

		Iterator<String> it = files.keySet().iterator();
		if (it.hasNext()) {
			CommonsMultipartFile file = files.get(it.next());

			String filename = getRandomFileName(md, file.getOriginalFilename(),
					FilenameUtil.getExtension(file.getOriginalFilename()));

			File path = new File(getMediaPath(riwUpload, file.getContentType()));
			path.mkdirs();
			File f = new File(path + File.separator + filename);

			FileItem fi = file.getFileItem();
			try {
				fi.write(f);
			} catch (Exception e) {
				e.printStackTrace();
				return CourseComponents.DATA_SAVING_FAIL;
			}
		}
		return CourseComponents.DATA_SAVED;
	}

	private String getMediaPath(String path, String contenType) {

		if (contenType.indexOf("image") >= 0) {
			path += File.separator + "img";
		} else if (contenType.indexOf("audio") >= 0) {
			path += File.separator + "audio";
		} else if (contenType.indexOf("zip") >= 0) {
			path += File.separator + "zip";
		}
		return path;
	}

	private String getRandomString(MessageDigest md, String seed) {
		md.update(seed.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();

		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}

		return sb.toString();
	}

	private String getRandomFileName(MessageDigest md, String original,
			String ext) {
		String seed = original + Long.toString(System.currentTimeMillis());

		return getRandomString(md, seed) + "." + ext;
	}

}
