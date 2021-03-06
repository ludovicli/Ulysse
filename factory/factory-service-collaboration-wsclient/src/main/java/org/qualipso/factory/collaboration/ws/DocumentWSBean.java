package org.qualipso.factory.collaboration.ws;

import java.util.HashMap;

import javax.ejb.Stateless;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qualipso.factory.collaboration.ws.beans.DocumentDTO;

@Stateless(name = "DocumentWSBean", mappedName = "DocumentWService")
public class DocumentWSBean extends CollaborationWSUtils implements
	DocumentWService {
    public DocumentWSBean() {

    }

    private EndpointReference targetEPR = new EndpointReference(
	    CollaborationProperties.getInstance().MERMIG_ENDPOINT);
    private static OMFactory fac = OMAbstractFactory.getOMFactory();
    private static OMNamespace omNs = fac.createOMNamespace(
	    CollaborationWSUtils.NAME_SPACE, "ns");
    private static Log logger = LogFactory.getLog(DocumentWSBean.class);

    @Override
    public HashMap<String, String> createDocument(String parentFolderId,
	    String name, String date, String type, String keywords,
	    String version, String status, String docname, String mimeType,
	    byte[] binaryContent) throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	OMElement payload = getCreateDocumentPayLoad(parentFolderId, name,
		date, type, keywords, version, status, docname, mimeType,
		binaryContent);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:createDocument");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result.toString());
	OMElement omStatusCode = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatusCode.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	if (omStatusCode.getText().equals(SUCCESS_CODE)) {
	    OMElement omDocumentID = result.getFirstChildWithName(OMEUtils
		    .getQName("documentID"));
	    values.put("documentId", omDocumentID.getText());
	}
	return values;
    }

    @Override
    public HashMap<String, Object> readDocument(String docId) throws Exception {
	HashMap<String, Object> values = new HashMap<String, Object>();
	values = callReadDocument(docId, false, values);
	values = callReadDocument(docId, true, values);
	return values;
    }

    @Override
    public HashMap<String, Object> readDocumentProperties(String docId)
	    throws Exception {
	return callReadDocument(docId, false, null);
    }

    private HashMap<String, Object> callReadDocument(String docId,
	    boolean getContent, HashMap<String, Object> values)
	    throws Exception {
	if (values == null) {
	    values = new HashMap<String, Object>();
	}
	OMElement payload = getReadDocumentPayLoad(docId, getContent);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	if (!getContent) {
	    options.setAction("urn:getDocumentProperties");
	} else {
	    options.setAction("urn:getDocument");
	}
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result.toString());
	OMElement omStatusCode = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatusCode.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	if (omStatusCode.getText().equals(SUCCESS_CODE)) {
	    DocumentDTO doc = null;

	    if (!getContent) {
		OMElement omDocProp = result.getFirstChildWithName(OMEUtils
			.getQName("documentProperties"));
		if (omDocProp != null) {
		    doc = new DocumentDTO();
		    doc.setId(docId);
		    //
		    // OMElement omDocName =
		    // omDocProp.getFirstChildWithName(OMEUtils.getQName("documentName"));
		    // doc.setName(omDocName.getText());
		    // OMElement omDocSubj =
		    // omDocProp.getFirstChildWithName(OMEUtils.getQName("documentSubject"));
		    // doc.setName(omDocSubj.getText());
		    OMElement omDocDate = omDocProp
			    .getFirstChildWithName(OMEUtils
				    .getQName("documentDate"));
		    doc.setDate(omDocDate.getText());
		    OMElement omDocSatus = omDocProp
			    .getFirstChildWithName(OMEUtils.getQName("status"));
		    doc.setStatus(omDocSatus.getText());
		    OMElement omDocVersion = omDocProp
			    .getFirstChildWithName(OMEUtils.getQName("version"));
		    doc.setVersion(omDocVersion.getText());
		    OMElement omDocKeywords = omDocProp
			    .getFirstChildWithName(OMEUtils
				    .getQName("keywords"));
		    doc.setKeywords(omDocKeywords.getText());
		    OMElement omDocSize = omDocProp
			    .getFirstChildWithName(OMEUtils.getQName("size"));
		    doc.setSize(omDocSize.getText());
		    OMElement omDocType = omDocProp
			    .getFirstChildWithName(OMEUtils.getQName("type"));
		    doc.setType(omDocType.getText());
		    // OMElement omDocAuthor =
		    // omDocProp.getFirstChildWithName(OMEUtils.getQName("author"));
		    // doc.setAuthor(omDocAuthor.getText());
		    OMElement omDocMime = omDocProp
			    .getFirstChildWithName(OMEUtils
				    .getQName("mimeType"));
		    doc.setMimeType(omDocMime.getText());
		    values.put("document", doc);
		}
	    } else {
		if (result.getFirstChildWithName(OMEUtils
			.getQName("documentContent")) != null) {
		    doc = new DocumentDTO();

		    OMElement omDocMime = result.getFirstChildWithName(OMEUtils
			    .getQName("mimeType"));
		    doc.setMimeType(omDocMime.getText());
		    OMElement omDocFile = result.getFirstChildWithName(OMEUtils
			    .getQName("fileName"));
		    doc.setFileName(omDocFile.getText());
		    OMElement omDocBytes = result
			    .getFirstChildWithName(OMEUtils
				    .getQName("documentContent"));
		    doc.setBinaryContent(omDocBytes.getText().getBytes());
		    doc.setId(docId);
		    values.put("documentContent", doc);
		}
	    }

	}

	return values;
    }

    @Override
    public HashMap<String, String> updateDocument(String documentID,
	    String name, String type, String keywords, String status,
	    String fileName, String mimeType, byte[] binaryContent)
	    throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	OMElement payload = getCreateUpdateDocumentPayLoad(documentID, true,
		null, name, null, type, keywords, null, status, fileName,
		mimeType, binaryContent);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:updateDocument");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result.toString());
	OMElement omStatusCode = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatusCode.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	return values;
    }

    @Override
    public HashMap<String, String> uploadDocumentVersion(String documentID,
	    String name, String version, String status, String fileName,
	    String mimeType, byte[] binaryContent) throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	OMElement payload = getUploadVersionPayLoad(documentID, name, version,
		status, fileName, mimeType, binaryContent);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:updateDocument");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result.toString());
	OMElement omStatusCode = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatusCode.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	return values;
    }

    private OMElement getUploadVersionPayLoad(String documentID, String name,
	    String version, String status, String fileName, String mimeType,
	    byte[] binaryContent) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public HashMap<String, String> deleteDocument(String id) throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	OMElement payload = getDeleteDocumentPayLoad(id);
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:deleteDocument");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result.toString());
	OMElement omStatusCode = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatusCode.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	return values;
    }

    @Override
    public HashMap<String, String> createFolder(String name,
	    String parentFolderID, String abstractText) throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	//
	OMElement payload = getCreateFolderPayLoad(name, parentFolderID,
		abstractText);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:createFolder");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result.toString());
	values.put("result", result.toString());
	OMElement omStatus = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatus.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	if (omStatus.getText().equals(SUCCESS_CODE)) {
	    OMElement omFolderID = result.getFirstChildWithName(OMEUtils
		    .getQName("folderID"));
	    values.put("folderID", omFolderID.getText());
	}
	return values;
    }

    @Override
    public HashMap<String, String> readFolder(String folderId) throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	//
	OMElement payload = getReadFolderPayLoad(folderId);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:getFolderProperties");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result);
	values.put("result", result.toString());
	OMElement omStatus = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatus.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	if (omStatus.getText().equals(SUCCESS_CODE)) {
	    OMElement omFolder = result.getFirstChildWithName(OMEUtils
		    .getQName("folderProperties"));
	    // OMElement omTitle =
	    // omFolder.getFirstChildWithName(OMEUtils.getQName("title"));
	    // values.put("title", omTitle.getText());
	    OMElement omAbstract = omFolder.getFirstChildWithName(OMEUtils
		    .getQName("abstract"));
	    values.put("abstract", omAbstract.getText());
	    OMElement omDateCreated = omFolder.getFirstChildWithName(OMEUtils
		    .getQName("dateCreated"));
	    values.put("dateCreated", omDateCreated.getText());
	}
	return values;
    }

    @Override
    public HashMap<String, String> updateFolder(String folderId, String name,
	    String abstractText) throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	//
	OMElement payload = getCreateUpdateFolderPayLoad(folderId, true, name,
		null, abstractText);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:updateFolder");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result);
	values.put("result", result.toString());
	OMElement omStatus = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatus.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	return values;
    }

    @Override
    public HashMap<String, String> deleteFolder(String folderId)
	    throws Exception {
	HashMap<String, String> values = new HashMap<String, String>();
	//
	OMElement payload = getDeleteFolderPayLoad(folderId);
	logger.info(payload.toString());
	Options options = new Options();
	options.setTo(targetEPR);
	options.setAction("urn:deleteFolder");
	// Blocking invocation
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(payload);
	logger.info(result);
	values.put("result", result.toString());
	OMElement omStatus = result.getFirstChildWithName(OMEUtils
		.getQName("statusCode"));
	values.put("statusCode", omStatus.getText());
	OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		.getQName("statusMessage"));
	values.put("statusMessage", omStatusMsg.getText());
	return values;
    }

    @Override
    public HashMap<String, Object> searchForDocument(String[][] metadataFilter,
	    String[] versionFilter) throws Exception {
	HashMap<String, Object> values = new HashMap<String, Object>();
	//
	if ((metadataFilter != null && metadataFilter.length > 0)
		|| (versionFilter != null && versionFilter.length == 2)) {
	    OMElement payload = getSearchPayload(metadataFilter, versionFilter);
	    logger.info(payload.toString());
	    Options options = new Options();
	    options.setTo(targetEPR);
	    options.setAction("urn:searchForDocument");
	    // Blocking invocation
	    ServiceClient sender = new ServiceClient();
	    sender.setOptions(options);
	    OMElement result = sender.sendReceive(payload);
	    logger.info(result);
	    values.put("result", result.toString());
	    OMElement omStatus = result.getFirstChildWithName(OMEUtils
		    .getQName("statusCode"));
	    values.put("statusCode", omStatus.getText());
	    OMElement omStatusMsg = result.getFirstChildWithName(OMEUtils
		    .getQName("statusMessage"));
	    values.put("statusMessage", omStatusMsg.getText());
	    if (omStatus.getText().equals(SUCCESS_CODE)) {
		// documentsList
		// document
		// documentID
		// documentResourceID
		// documentName
		// documentSubject
		// documentAuthor
		// documentDate
	    }
	}
	return values;
    }

    private static OMElement getCreateDocumentPayLoad(String parentFolderId,
	    String name, String date, String type, String keywords,
	    String version, String status, String docname, String mimeType,
	    byte[] binaryContent) {
	return getCreateUpdateDocumentPayLoad(null, false, parentFolderId,
		name, date, type, keywords, version, status, docname, mimeType,
		binaryContent);
    }

    private static OMElement getCreateUpdateDocumentPayLoad(String documentID,
	    boolean isUpdate, String parentFolderId, String name, String date,
	    String type, String keywords, String version, String status,
	    String docname, String mimeType, byte[] binaryContent) {
	OMElement method;
	if (!isUpdate) {
	    method = fac.createOMElement("createDocument", omNs);
	} else {
	    method = fac.createOMElement("updateDocument", omNs);
	}

	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	method.addChild(userElement);
	method.addChild(pwdElement);
	if (!isUpdate) {
	    OMElement idElement = fac.createOMElement("workspaceID", omNs);
	    idElement.addChild(fac.createOMText(idElement,
		    DEFAULT_WORKSPACE_STR));
	    method.addChild(idElement);
	    if (parentFolderId != null) {
		OMElement parentElement = fac.createOMElement("parentFolderID",
			omNs);
		parentElement.addChild(fac.createOMText(parentElement,
			parentFolderId));
		method.addChild(parentElement);
	    }
	}
	// documentProperties
	OMElement docElement = fac.createOMElement("documentProperties", omNs);
	if (isUpdate && documentID != null) {
	    OMElement idElement = fac.createOMElement("documentID", omNs);
	    idElement.addChild(fac.createOMText(idElement, documentID));
	    docElement.addChild(idElement);
	}
	OMElement subjectElement = fac.createOMElement("documentSubject", omNs);
	subjectElement.addChild(fac.createOMText(subjectElement, name));
	docElement.addChild(subjectElement);
	if (!isUpdate) {
	    OMElement versionElement = fac.createOMElement("version", omNs);
	    versionElement.addChild(fac.createOMText(versionElement, version));
	    docElement.addChild(versionElement);
	}
	OMElement statusElement = fac.createOMElement("status", omNs);
	statusElement.addChild(fac.createOMText(statusElement, status));
	docElement.addChild(statusElement);
	if (date != null && !isUpdate) {
	    OMElement dateElement = fac.createOMElement("date", omNs);
	    dateElement.addChild(fac.createOMText(dateElement, date));
	    docElement.addChild(dateElement);
	}
	OMElement typeElement = fac.createOMElement("type", omNs);
	typeElement.addChild(fac.createOMText(typeElement, type));
	docElement.addChild(typeElement);
	if (isUpdate) {
	    OMElement authorElement = fac.createOMElement("author", omNs);
	    authorElement.addChild(fac.createOMText(authorElement, USER_NAME));
	    docElement.addChild(authorElement);
	}
	if (keywords != null) {
	    OMElement keywordsElement = fac.createOMElement("keywords", omNs);
	    keywordsElement.addChild(fac
		    .createOMText(keywordsElement, keywords));
	    docElement.addChild(keywordsElement);
	}
	// in update the upload is optional
	if (docname != null && !docname.equals("") && binaryContent != null
		&& binaryContent.length > 0) {
	    OMElement uploadFileElement = fac.createOMElement("uploadFile",
		    omNs);
	    OMElement fileNameElement = fac.createOMElement("fileName", omNs);
	    fileNameElement
		    .addChild(fac.createOMText(fileNameElement, docname));
	    OMElement mimeElement = fac.createOMElement("mimeType", omNs);
	    mimeElement.addChild(fac.createOMText(mimeElement, mimeType));
	    OMElement contentElement = fac.createOMElement("binaryContent",
		    omNs);
	    contentElement.addChild(fac.createOMText(contentElement,
		    new String(binaryContent)));
	    //
	    uploadFileElement.addChild(fileNameElement);
	    uploadFileElement.addChild(mimeElement);
	    uploadFileElement.addChild(contentElement);
	    docElement.addChild(uploadFileElement);
	}
	//
	method.addChild(docElement);
	return method;
    }

    private static OMElement getReadDocumentPayLoad(String id,
	    boolean getContent) {
	OMElement method;
	if (!getContent) {
	    method = fac.createOMElement("getDocumentProperties", omNs);
	} else {
	    method = fac.createOMElement("getDocument", omNs);
	}
	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	OMElement idElement = fac.createOMElement("documentID", omNs);
	idElement.addChild(fac.createOMText(idElement, id));
	//
	method.addChild(userElement);
	method.addChild(pwdElement);
	method.addChild(idElement);
	return method;
    }

    private static OMElement getDeleteDocumentPayLoad(String id) {

	OMElement method = fac.createOMElement("deleteDocument", omNs);
	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	OMElement idElement = fac.createOMElement("documentID", omNs);
	idElement.addChild(fac.createOMText(idElement, id));
	method.addChild(userElement);
	method.addChild(pwdElement);
	method.addChild(idElement);

	return method;
    }

    private static OMElement getCreateFolderPayLoad(String name,
	    String parentFolderID, String abstractText) {
	return getCreateUpdateFolderPayLoad(null, false, name, parentFolderID,
		abstractText);
    }

    private static OMElement getCreateUpdateFolderPayLoad(String folderID,
	    boolean isUpdate, String name, String parentFolderID,
	    String abstractText) {
	OMElement method;
	if (!isUpdate) {
	    method = fac.createOMElement("createFolder", omNs);
	} else {
	    method = fac.createOMElement("updateFolder", omNs);
	}
	//
	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	method.addChild(userElement);
	method.addChild(pwdElement);

	//
	OMElement titleElement = fac.createOMElement("folderTitle", omNs);
	titleElement.addChild(fac.createOMText(titleElement, name));
	if (!isUpdate) {
	    OMElement idElement = fac.createOMElement("workspaceID", omNs);
	    idElement.addChild(fac.createOMText(idElement,
		    DEFAULT_WORKSPACE_STR));
	    method.addChild(idElement);
	    if (parentFolderID != null) {
		OMElement parentElement = fac.createOMElement("parentFolderID",
			omNs);
		parentElement.addChild(fac.createOMText(pwdElement,
			parentFolderID));
		method.addChild(parentElement);
	    }
	    method.addChild(titleElement);
	    if (abstractText != null) {
		OMElement abstractElement = fac.createOMElement(
			"folderAbstract", omNs);
		abstractElement.addChild(fac.createOMText(abstractElement,
			abstractText));
		method.addChild(abstractElement);
	    }
	} else if (isUpdate && folderID != null) {
	    OMElement fpElement = fac.createOMElement("folderProperties", omNs);
	    OMElement idElement = fac.createOMElement("folderID", omNs);
	    idElement.addChild(fac.createOMText(idElement, folderID));
	    fpElement.addChild(idElement);
	    fpElement.addChild(titleElement);
	    if (abstractText != null) {
		OMElement abstractElement = fac.createOMElement(
			"folderAbstract", omNs);
		abstractElement.addChild(fac.createOMText(abstractElement,
			abstractText));
		fpElement.addChild(abstractElement);
	    }
	    method.addChild(fpElement);
	}

	return method;
    }

    private static OMElement getReadFolderPayLoad(String id) {
	OMElement method = fac.createOMElement("getFolderProperties", omNs);
	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	OMElement idElement = fac.createOMElement("folderID", omNs);
	idElement.addChild(fac.createOMText(idElement, id));
	method.addChild(userElement);
	method.addChild(pwdElement);
	method.addChild(idElement);

	return method;
    }

    private static OMElement getDeleteFolderPayLoad(String id) {
	OMElement method = fac.createOMElement("deleteFolder", omNs);
	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	OMElement idElement = fac.createOMElement("folderID", omNs);
	idElement.addChild(fac.createOMText(idElement, id));
	method.addChild(userElement);
	method.addChild(pwdElement);
	method.addChild(idElement);

	return method;
    }

    private static OMElement getSearchPayload(String[][] metadataFilter,
	    String[] versionFilter) {
	OMElement method = fac.createOMElement("deleteDocument", omNs);
	OMElement userElement = fac.createOMElement("username", omNs);
	userElement.addChild(fac.createOMText(userElement, USER_NAME));
	OMElement pwdElement = fac.createOMElement("password", omNs);
	pwdElement.addChild(fac.createOMText(pwdElement, USER_PWD));
	OMElement idElement = fac.createOMElement("workspaceID", omNs);
	idElement.addChild(fac.createOMText(idElement, DEFAULT_WORKSPACE_STR));
	method.addChild(userElement);
	method.addChild(pwdElement);
	method.addChild(idElement);
	if (metadataFilter != null && metadataFilter.length > 0
		&& metadataFilter.length < 4) {
	    for (int i = 0; i < metadataFilter.length; i++) {
		if (metadataFilter[i].length == 3) {
		    OMElement metadataFilterElement = fac.createOMElement(
			    "metadataFilter", omNs);
		    OMElement fElement = fac.createOMElement("filterField",
			    omNs);
		    fElement.addChild(fac.createOMText(fElement,
			    metadataFilter[i][0]));
		    metadataFilterElement.addChild(fElement);
		    fElement = fac.createOMElement("filterValue", omNs);
		    fElement.addChild(fac.createOMText(fElement,
			    metadataFilter[i][1]));
		    metadataFilterElement.addChild(fElement);
		    fElement = fac.createOMElement("filterOperator", omNs);
		    fElement.addChild(fac.createOMText(fElement,
			    metadataFilter[i][2]));
		    metadataFilterElement.addChild(fElement);
		    method.addChild(metadataFilterElement);
		}
	    }
	}
	if (versionFilter != null && versionFilter.length == 1) {
	    OMElement versionFilterElement = fac.createOMElement(
		    "versionFilter", omNs);
	    OMElement fElement = fac.createOMElement("filterValue", omNs);
	    fElement.addChild(fac.createOMText(fElement, versionFilter[0]));
	    versionFilterElement.addChild(fElement);
	    fElement = fac.createOMElement("filterOperator", omNs);
	    fElement.addChild(fac.createOMText(fElement, versionFilter[1]));
	    versionFilterElement.addChild(fElement);
	    method.addChild(versionFilterElement);
	}

	return method;
    }
}
