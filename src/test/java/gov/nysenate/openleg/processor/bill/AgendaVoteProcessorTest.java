package gov.nysenate.openleg.processor.bill;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.openleg.dao.agenda.data.AgendaDao;
import gov.nysenate.openleg.model.agenda.*;
import gov.nysenate.openleg.model.base.SessionYear;
import gov.nysenate.openleg.model.bill.BillId;
import gov.nysenate.openleg.model.bill.BillVote;
import gov.nysenate.openleg.model.bill.BillVoteType;
import gov.nysenate.openleg.model.entity.Chamber;
import gov.nysenate.openleg.model.entity.CommitteeId;
import gov.nysenate.openleg.model.entity.SessionMember;
import gov.nysenate.openleg.model.sourcefiles.sobi.SobiFragment;
import gov.nysenate.openleg.processor.BaseXmlProcessorTest;
import gov.nysenate.openleg.processor.agenda.AgendaVoteProcessor;
import gov.nysenate.openleg.processor.sobi.SobiProcessor;
import gov.nysenate.openleg.service.entity.member.data.MemberService;
import gov.nysenate.openleg.util.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by uros on 4/12/17.
 *
 */
@Transactional
public class AgendaVoteProcessorTest extends BaseXmlProcessorTest {

    ObjectMapper mapper = new ObjectMapper();
    @Autowired private AgendaDao agendaDao;
    @Autowired private AgendaVoteProcessor agendaVoteProcessor;
    @Autowired private MemberService memberService;


    @Override
    protected SobiProcessor getSobiProcessor() {
        return agendaVoteProcessor;
    }

    @Test
    public void processSenAgendaVote() throws JsonProcessingException {
        AgendaId agendaId = new AgendaId(5, 2017);
        agendaDao.deleteAgenda(agendaId);

        String xmlPath = "processor/bill/senAgendaVote/2017-02-06-16.54.35.038848_SENAGENV_RULES.XML";

        SobiFragment sobiFragment = generateXmlSobiFragment(xmlPath);
        processFragment(sobiFragment);

        Agenda agenda = agendaDao.getAgenda(agendaId);
        AgendaVoteAddendum actual = agenda.getAgendaVoteAddendum("C");

        AgendaVoteAddendum expected = new AgendaVoteAddendum();
        expected.setAgendaId(agendaId);
        expected.setId("C");
        expected.setPublishedDateTime(sobiFragment.getPublishedDateTime());
        expected.setModifiedDateTime(sobiFragment.getPublishedDateTime());
        CommitteeId committeeId = new CommitteeId(Chamber.SENATE, "Rules");
        String chair = "John J. Flanagan";
        LocalDateTime meetDataTime = DateUtils.getLrsDateTime("2017-02-06T00.00.00Z");
        AgendaVoteCommittee voteCommittee = new AgendaVoteCommittee(committeeId, chair, meetDataTime);
        SessionYear sessionYear = new SessionYear(2017);
        SessionMember member = memberService.getMemberByShortNameEnsured("Flanagan",sessionYear,Chamber.SENATE);
        AgendaVoteAttendance memberAttendance = new AgendaVoteAttendance(member,1,"R","Present");
        SessionMember member1 = memberService.getMemberByShortNameEnsured("DeFrancisco",sessionYear,Chamber.SENATE);
        AgendaVoteAttendance memberAttendance1 = new AgendaVoteAttendance(member1,2,"R","Present");
        voteCommittee.addAttendance(memberAttendance);
        voteCommittee.addAttendance(memberAttendance1);
        expected.putCommittee(voteCommittee);
        Map<BillId, AgendaVoteBill> voteBillMap = new TreeMap<>();
        AgendaVoteBill agendaVoteBill1 =  new AgendaVoteBill(AgendaVoteAction.THIRD_READING, new CommitteeId(Chamber.SENATE,"Rules"),false);
        agendaVoteBill1.setBillVote(new BillVote(new BillId("S2956A",2017),LocalDate.of(2017,2,6), BillVoteType.COMMITTEE));
        AgendaVoteBill agendaVoteBill2 = new AgendaVoteBill(AgendaVoteAction.THIRD_READING, new CommitteeId(Chamber.SENATE,"Rules"),false);
        agendaVoteBill2.setBillVote(new BillVote(new BillId("S3505",2017),LocalDate.of(2017,2,6), BillVoteType.COMMITTEE));
        voteBillMap.put(new BillId("S2956A",2017),agendaVoteBill1);
        voteBillMap.put(new BillId("S3505",2017), agendaVoteBill2);
        voteCommittee.setVotedBills(voteBillMap);
        Map<CommitteeId, AgendaVoteCommittee> committeeIdAgendaVoteCommitteeHashMap = new HashMap<>();
        committeeIdAgendaVoteCommitteeHashMap.put(new CommitteeId(Chamber.SENATE,"Rules"),voteCommittee);
        expected.setCommitteeVoteMap(committeeIdAgendaVoteCommitteeHashMap);

        assertEquals(expected.getAgendaId(),actual.getAgendaId());
        assertEquals(expected.getId(),actual.getId());
        assertEquals(expected.getModifiedDateTime(),actual.getModifiedDateTime());
        assertEquals(expected.getPublishedDateTime(),actual.getPublishedDateTime());
        assertEquals(expected.getSession(),actual.getSession());
        assertEquals(expected.getYear(),actual.getYear());

        /**
         * Compare AgendaVoteCommittee
         */
        AgendaVoteCommittee expectedAgendaVoteCommittee = expected.getCommitteeVoteMap().get(new CommitteeId(Chamber.SENATE,"Rules"));
        AgendaVoteCommittee actualAgendaVoteCommittee = actual.getCommitteeVoteMap().get(new CommitteeId(Chamber.SENATE,"Rules"));

        assertEquals(expectedAgendaVoteCommittee.getAttendance(),actualAgendaVoteCommittee.getAttendance());
        assertEquals(expectedAgendaVoteCommittee.getChair(), actualAgendaVoteCommittee.getChair());
        assertEquals(expectedAgendaVoteCommittee.getMeetingDateTime(), actualAgendaVoteCommittee.getMeetingDateTime());
        assertEquals(expectedAgendaVoteCommittee.getCommitteeId(), actualAgendaVoteCommittee.getCommitteeId());

    }
}
