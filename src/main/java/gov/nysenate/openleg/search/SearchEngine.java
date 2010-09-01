package gov.nysenate.openleg.search;

import gov.nysenate.openleg.PMF;
import gov.nysenate.openleg.OpenLegConstants;
import gov.nysenate.openleg.lucene.Lucene;
import gov.nysenate.openleg.lucene.LuceneObject;
import gov.nysenate.openleg.lucene.LuceneSerializer;
import gov.nysenate.openleg.model.Bill;
import gov.nysenate.openleg.model.BillEvent;
import gov.nysenate.openleg.model.Person;
import gov.nysenate.openleg.model.Transcript;
import gov.nysenate.openleg.model.Vote;
import gov.nysenate.openleg.model.calendar.Calendar;
import gov.nysenate.openleg.model.calendar.Section;
import gov.nysenate.openleg.model.calendar.Supplemental;
import gov.nysenate.openleg.model.committee.Addendum;
import gov.nysenate.openleg.model.committee.Agenda;
import gov.nysenate.openleg.model.committee.Meeting;
import gov.nysenate.openleg.util.JsonConverter;
import gov.nysenate.openleg.util.OriginalApiConverter;
import gov.nysenate.openleg.xstream.XStreamBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public abstract class SearchEngine extends Lucene implements OpenLegConstants {
	
	protected DateFormat DATE_FORMAT_MEDIUM = java.text.DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    public void deleteSenateObject (Object obj) throws Exception
    {
    	if (obj instanceof Agenda) {
    		Agenda agenda = (Agenda)obj;
    		if (agenda.getAddendums() != null)
	    		for( Addendum addendum : agenda.getAddendums() ) 
	    			for( Meeting meeting : addendum.getMeetings() ) {
	    				deleteSenateObject( meeting );
	    			}
    	}
    	else if (obj instanceof Bill) {
            deleteSenateObjectById("bill",((Bill)obj).getSenateBillNo());
    	}
    	else if (obj instanceof Supplemental) {
    		deleteSenateObjectById("calendar",((Supplemental)obj).getCalendar().getId());
    	}
    	else if (obj instanceof Meeting) { 
    		deleteSenateObjectById("meeting",((Meeting)obj).getId());
		}
    	else if (obj instanceof Transcript) {
    		deleteSenateObjectById("transcript",((Transcript)obj).getId());
		}
    	else if (obj instanceof Vote) {
    		deleteSenateObjectById("vote",((Vote)obj).getId());
		}
    	else if (obj instanceof BillEvent) {
    		deleteSenateObjectById("action",((BillEvent)obj).getBillEventId());
    	}
    }
    
    public void deleteSenateObjectById (String type, String id) throws Exception {
    	closeSearcher();
    	deleteDocuments(type, id);
    	openSearcher();
    }
	
	public void indexSenateData(String type, LuceneSerializer ls) throws Exception
	{		
		if (type.equals("transcripts") || type.equals("*"))	{
			doIndex("transcript", Transcript.class, null, 25, ls);
		}
		
		if (type.equals("meetings") || type.equals("*"))	{
			doIndex("meeting", Meeting.class, null, 10, ls);
		}
		
		if (type.equals("calendars") || type.equals("*"))	{
			doIndex("calendar", Calendar.class, null, 25, ls);
		}
		
		if (type.equals("bills") || type.equals("*"))	{
			doIndex("bill", Bill.class, SORTINDEX_DESCENDING, 25, ls);
		}
		
		if (type.equals("billevents") || type.equals("*"))	{
			doIndex("billevent", BillEvent.class, null, 25, ls);
		}
		
		if (type.equals("votes") || type.equals("*"))	{
			doIndex("vote", Vote.class, null, 25, ls);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void doIndex(String type, Class objClass, String sort, int pageSize, LuceneSerializer ls) throws IOException {	
		int start = 1;
		int end = start+pageSize;
		
		Collection<LuceneObject> result = null;
		
		deleteDocuments(type,null);
		
		do {			
			System.out.println(type + " : " + start);
			result = (Collection<LuceneObject>)PMF.getDetachedObjects(objClass, sort, start, end);
			indexSenateObjects(result, ls);
			start += pageSize;
			end = start+pageSize;
		}
		while (result.size() == pageSize);
		
	}	
	
    public  boolean indexSenateObjects (Collection<LuceneObject> objects, LuceneSerializer ls) throws IOException
    {
    	createIndex ();
        Analyzer  analyzer    = new StandardAnalyzer(Version.LUCENE_CURRENT);
        IndexWriter indexWriter = new IndexWriter(getDirectory(), analyzer, false, MaxFieldLength.UNLIMITED);
       
    	Iterator<LuceneObject> it = objects.iterator();
    	while (it.hasNext()) {
    		LuceneObject obj = it.next();
    		
    		if (obj instanceof Calendar) {
    			Calendar cal = (Calendar)obj;
    			
    			Iterator<Supplemental> itSupps = cal.getSupplementals().iterator();
    			while (itSupps.hasNext()) {
    				Supplemental supp = (Supplemental)itSupps.next();
    				supp.setCalendar(cal);
    				
    				try {
    	    			addDocument(supp, ls, indexWriter);
    	    		}
    	    		catch (Exception e) {
    	    			logger.warn("unable to index senate supp",e);
    	    		}
    			}
    		}
    		else {
	    		try {
	    			addDocument(obj, ls, indexWriter);
	    		}
	    		catch (Exception e) {
	    			logger.warn("unable to index senate object: " + obj.getClass().getName(),e);
	    		}
	    	}
    	}
    	
    	logger.info("done indexing objects(" + objects.size() + ". Closing index.");
    	indexWriter.close();
    	return true;
    }
	
}