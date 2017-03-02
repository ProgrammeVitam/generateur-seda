/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 * 
 * This software is a computer program whose purpose is to implement a digital 
 * archiving back-office system managing high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL 2.1
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL 2.1 license and that you accept its terms.
 */
package fr.gouv.vitam.generator.seda.core;

import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import fr.gouv.culture.archivesdefrance.seda.v2.AccessRuleType;
import fr.gouv.culture.archivesdefrance.seda.v2.AppraisalRuleType;
import fr.gouv.culture.archivesdefrance.seda.v2.ClassificationRuleType;
import fr.gouv.culture.archivesdefrance.seda.v2.CoverageType;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentType;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.DisseminationRuleType;
import fr.gouv.culture.archivesdefrance.seda.v2.EventType;
import fr.gouv.culture.archivesdefrance.seda.v2.FinalActionAppraisalCodeType;
import fr.gouv.culture.archivesdefrance.seda.v2.FinalActionStorageCodeType;
import fr.gouv.culture.archivesdefrance.seda.v2.GpsType;
import fr.gouv.culture.archivesdefrance.seda.v2.IdentifierType;
import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.culture.archivesdefrance.seda.v2.ManagementMetadataTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.OrganizationType;
import fr.gouv.culture.archivesdefrance.seda.v2.ReuseRuleType;
import fr.gouv.culture.archivesdefrance.seda.v2.RuleIdType;
import fr.gouv.culture.archivesdefrance.seda.v2.StorageRuleType;
import fr.gouv.culture.archivesdefrance.seda.v2.TextType;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.PlaybookBuilder;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;

/**
 * 
 */
public class ArchiveTransferGeneratorImplTest {
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ArchiveTransferGeneratorImplTest.class);

    private static final String OUTPUT_FILE = "output.zip";

    @Test
    public void correctSeda() {
        try {
            // TODO FORMAT (sur tous les fichiers)

            // TODO Helper dans PropertiesUtils
            ClassLoader classLoader = getClass().getClassLoader();
            String headerPath = classLoader.getResource("sip1.json").getFile();
            String configDir = classLoader.getResource("conf").getFile();
            ArchiveTransferConfig atc = new ArchiveTransferConfig("/", configDir);
            ArchiveTransferGenerator atgi = new ArchiveTransferGenerator(atc, OUTPUT_FILE);
            atgi.generateHeader();
            String archiveFatherID = atgi.addArchiveUnit("Titre0", "Description0");
            String archiveSonID1 = atgi.addArchiveUnit("Titre1", "Description1");
            String archiveSonID2 = atgi.addArchiveUnit("Titre2", "Description2");
            String archiveSonID3 = atgi.addArchiveUnit("Titre3", "Description3");
            String archiveSonID4 = atgi.addArchiveUnit("Titre4", "Description4");
            atgi.setTransactedDate(archiveSonID1, new Date(2));
            atgi.setTransactedDate(archiveSonID3, new Date(1));
            atgi.setTransactedDate(archiveSonID4, new Date());

            atgi.setTransactedDate(archiveSonID1, new Date());
            atgi.removeArchiveUnit(archiveSonID2);
            String dataObjectGroup1ID = atgi.getDataObjectGroupUsedMap().registerDataObjectGroup();
            atgi = addBinaryDataObject(atgi, headerPath, dataObjectGroup1ID);
            atgi = addBinaryDataObject(atgi, headerPath, dataObjectGroup1ID);
            atgi.addArchiveUnit2ArchiveUnitReference(archiveFatherID, archiveSonID1);
            atgi.addArchiveUnit2ArchiveUnitReference(archiveFatherID, archiveSonID3);
            atgi.addArchiveUnit2ArchiveUnitReference(archiveFatherID, archiveSonID4);
            atgi.addArchiveUnit2DataObjectGroupReference(archiveSonID1, dataObjectGroup1ID);
            atgi.addRawContentFile(archiveSonID4,
                new File(classLoader.getResource("ArchiveUnitContent.xml").getFile()));
            atgi.addRawManagementFile(archiveFatherID,
                new File(classLoader.getResource("ArchiveUnitManagement.xml").getFile()));
            atgi.addStartAndEndDate2ArchiveUnit(archiveFatherID);
            atgi = addBinaryDataObject(atgi, headerPath, null);
            atgi.writeDescriptiveMetadata();
            atgi.writeManagementMetadata();
            atgi.closeDocument();
        } catch (Exception e) {
            LOGGER.error("Should not have an exception", e);
            fail("Should not have an exception");
        }

    }

    @Test
    public void importJsonMetadata() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            ArchiveTransferConfig atc =
                new ArchiveTransferConfig("/", classLoader.getResource("conf/ArchiveTransferConfig.json").getPath());
            ArchiveTransferGenerator atgi = new ArchiveTransferGenerator(atc, OUTPUT_FILE);
            atgi.generateHeader();
            File f = new File(classLoader.getResource("ArchiveUnitMetadata.json").getFile());
            atgi.addArchiveUnit("test", "test", f);
        } catch (Exception e) {
            LOGGER.error("Should not have an exception", e);
            fail("Should not have an exception");
        }
    }


    @Test
    public void emptyFile() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            ArchiveTransferConfig atc =
                new ArchiveTransferConfig("/", classLoader.getResource("conf/ArchiveTransferConfig.json").getPath());
            ArchiveTransferGenerator atgi = new ArchiveTransferGenerator(atc, OUTPUT_FILE);
            atgi.generateHeader();
            addBinaryDataObject(atgi, classLoader.getResource("empty").getFile(), null);
        } catch (VitamBinaryDataObjectException e) {
            return;
        } catch (VitamException | XMLStreamException e) {
            fail("The empty file should raise an VitamBinaryDataObjectException");
        }
        fail("The empty file should raise an exception");
    }

    @Test
    public void generateModelManagementMetadata() {
        ManagementMetadataTypeRoot mmtr = new ManagementMetadataTypeRoot();
        XMLGregorianCalendar xgc = null;
        try {
            xgc = XMLWriterUtils.getXMLGregorianCalendar(new Date());
        } catch (VitamSedaException e) {
            e.printStackTrace();
            fail();
        }
        StorageRuleType srt = new StorageRuleType();
        RuleIdType rit = new RuleIdType();
        rit.setId("43");
        rit.setValue("STO_01");
        srt.getRuleAndStartDate().add(rit);
        srt.getRuleAndStartDate().add(xgc);
        srt.setPreventInheritance(true);
        srt.setFinalAction(FinalActionStorageCodeType.TRANSFER);
        srt.getRefNonRuleId().add(rit);
        mmtr.setStorageRule(srt);
        AppraisalRuleType art = new AppraisalRuleType();
        RuleIdType rit1 = new RuleIdType();
        rit1.setValue("APR_01");
        art.getRuleAndStartDate().add(xgc);
        art.getRuleAndStartDate().add(rit1);
        art.setPreventInheritance(false);
        art.setFinalAction(FinalActionAppraisalCodeType.KEEP);
        art.getRefNonRuleId().add(rit1);
        mmtr.setAppraisalRule(art);
        AccessRuleType acrt = new AccessRuleType();
        RuleIdType rit2 = new RuleIdType();
        rit2.setValue("ACC_01");
        acrt.getRuleAndStartDate().add(rit2);
        acrt.getRuleAndStartDate().add(xgc);
        acrt.getRefNonRuleId().add(rit2);
        mmtr.setAccessRule(acrt);
        DisseminationRuleType drt = new DisseminationRuleType();
        RuleIdType rit3 = new RuleIdType();
        rit3.setValue("DIS_01");
        drt.getRuleAndStartDate().add(rit3);
        drt.getRuleAndStartDate().add(xgc);
        drt.getRefNonRuleId().add(rit3);
        mmtr.setDisseminationRule(drt);

        ReuseRuleType rrt = new ReuseRuleType();
        RuleIdType rit4 = new RuleIdType();
        rit4.setValue("REU_01");
        rrt.getRuleAndStartDate().add(rit4);
        rrt.getRuleAndStartDate().add(xgc);
        rrt.getRefNonRuleId().add(rit4);
        rrt.setPreventInheritance(true);
        mmtr.setReuseRule(rrt);

        ClassificationRuleType crt = new ClassificationRuleType();
        RuleIdType rit5 = new RuleIdType();
        rit5.setValue("CLA_01");
        crt.getRuleAndStartDate().add(rit5);
        crt.getRuleAndStartDate().add(xgc);
        crt.getRefNonRuleId().add(rit5);
        mmtr.setClassificationRule(crt);

        mmtr.setNeedAuthorization(true);


        try {
            System.out.println(JsonHandler.writeAsString(mmtr));
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void generateModelContentMetadata() {
        DescriptiveMetadataContentTypeRoot dmct = new DescriptiveMetadataContentTypeRoot();
        XMLGregorianCalendar xgc = null;
        try {
            xgc = XMLWriterUtils.getXMLGregorianCalendar(new Date());
        } catch (VitamSedaException e) {
            e.printStackTrace();
            fail();
        }
        dmct.setDescriptionLevel(LevelType.RECORD_GRP);
        TextType title_fr = new TextType();
        title_fr.setValue("Titre francais");
        title_fr.setLang("fr");
        dmct.getTitle().add(title_fr);
        TextType title_en = new TextType();
        title_en.setValue("English title");
        title_en.setLang("en");
        dmct.getTitle().add(title_en);
        dmct.setFilePlanPosition("Valeur de filePlanPosition");
        dmct.setSystemId("Valeur de SystemID)");
        dmct.setOriginatingSystemId("Valeur de OriginatingSystemId");
        dmct.setArchivalAgencyArchiveUnitIdentifier("Valeur de archivalAgencyArchiveUnitIdentifier");
        dmct.setOriginatingAgencyArchiveUnitIdentifier("Valeur de originatingAgencyArchiveUnitIdentifier");
        dmct.setTransferringAgencyArchiveUnitIdentifier("Valeur de transferringAgencyArchiveUnitIdentifier");
        TextType descriptionFR = new TextType();
        descriptionFR.setValue("Description francaise");
        descriptionFR.setLang("fr");
        dmct.getDescription().add(descriptionFR);
        TextType descriptionEN = new TextType();
        descriptionEN.setValue("English Description");
        descriptionEN.setLang("en");
        dmct.getDescription().add(descriptionEN);
        // custodialHistory non implémenté
        TextType type = new TextType();
        type.setValue("Valeur du type");
        type.setLang("fr");
        dmct.setType(type);
        TextType documentType = new TextType();
        documentType.setValue("Valeur du document");
        documentType.setValue("fr");
        dmct.setDocumentType(documentType);
        dmct.setLanguage("FR");
        dmct.setDescriptionLanguage("FR");
        dmct.setStatus("Valeur de Status");
        dmct.setVersion("Valeur de version");
        dmct.getTag().add("XML Tag 1 (de type xml:token)");
        dmct.getTag().add("XML Tag 2 (de type xml:token)");
        // Keyword non implémenté
        CoverageType ct = new CoverageType();
        TextType spatial = new TextType();
        spatial.setValue("Valeur de Spatial");
        spatial.setLang("fr");
        ct.getSpatial().add(spatial);
        TextType temporal = new TextType();
        temporal.setValue("Valeur de temporal");
        temporal.setLang("fr");
        ct.getTemporal().add(temporal);
        TextType juridictional = new TextType();
        juridictional.setValue("Valeur de juridictional");
        juridictional.setLang("fr");
        ct.getJuridictional().add(juridictional);
        dmct.setCoverage(ct);
        IdentifierType it = new IdentifierType();
        it.setValue("Identifiant de l'OriginatingAgency");
        OrganizationType ot = new OrganizationType();
        ot.setIdentifier(it);
        dmct.setOriginatingAgency(ot);
        IdentifierType it2 = new IdentifierType();
        it2.setValue("Identifiant de l'OriginatingAgency");
        OrganizationType ot2 = new OrganizationType();
        ot2.setIdentifier(it2);
        dmct.setSubmissionAgency(ot2);
        DescriptiveMetadataContentType.Writer wr = new DescriptiveMetadataContentType.Writer();
        wr.setGivenName("Valeur de GivenName");
        wr.setFirstName("Valeur de FirstName");
        wr.setBirthName("Valeur de BirthName");
        wr.setBirthDate(xgc);
        wr.setDeathDate(xgc);
        dmct.getWriter().add(wr);
        dmct.setSource("Valeur de Source");
        // Related Object Reference non implémenté
        dmct.setCreatedDate(xgc.toString());
        dmct.setTransactedDate(xgc.toString());
        dmct.setAcquiredDate(xgc.toString());
        dmct.setSentDate(xgc.toString());
        dmct.setReceivedDate(xgc.toString());
        dmct.setStartDate("Start Date : A ne pas utiliser car calculé automatiquement par le générateur SEDA");
        dmct.setEndDate("End Date : A ne pas utiliser car calculé automatiquement par le générateur SEDA");
        EventType et = new EventType();
        et.setEventIdentifier("Identifiant de l'évenement");
        et.setEventType("Type de l'évenement");
        et.setEventDateTime(xgc);
        dmct.getEvent().add(et);
        // Signature non implémenté
        GpsType gps = new GpsType();
        gps.setGpsLatitude("Latitude : string sans formatage imposé par le SEDA");
        gps.setGpsLongitude("Longitude :  string sans formatage imposé par le SEDA");
        gps.setGpsAltitude(new BigInteger("8848"));
        dmct.setGps(gps);
        dmct.setRestrictionValue("Valeur de restrictionValue");
        dmct.setRestrictionEndDate(xgc);
        try {
            System.out.println(JsonHandler.writeAsString(dmct));
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail();
        }
    }

    private ArchiveTransferGenerator addBinaryDataObject(ArchiveTransferGenerator atgi, String filename,
        String dataObjectGroupID) throws VitamException {
        ParameterMap pm = new ParameterMap();
        pm.put("file", filename);
        pm.put("dataobjectgroupID", dataObjectGroupID);
        pm.put("archivetransfergenerator", atgi);
        ClassLoader classLoader = getClass().getClassLoader();
        String jsonFile = classLoader.getResource("playbook_BinaryDataObject.json").getFile();
        Playbook pb = PlaybookBuilder.getPlaybook(jsonFile);
        SchedulerEngine se = new SchedulerEngine();
        se.execute(pb, pm);
        return atgi;
    }

}
