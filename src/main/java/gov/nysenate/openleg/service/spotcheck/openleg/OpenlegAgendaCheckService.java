package gov.nysenate.openleg.service.spotcheck.openleg;

import gov.nysenate.openleg.client.view.agenda.AgendaCommAddendumView;
import gov.nysenate.openleg.model.agenda.CommitteeAgendaAddendumId;
import gov.nysenate.openleg.model.spotcheck.ReferenceDataNotFoundEx;
import gov.nysenate.openleg.model.spotcheck.SpotCheckMismatchType;
import gov.nysenate.openleg.model.spotcheck.SpotCheckObservation;
import gov.nysenate.openleg.service.spotcheck.base.BaseSpotCheckService;
import gov.nysenate.openleg.util.OutputUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OpenlegAgendaCheckService extends BaseSpotCheckService<CommitteeAgendaAddendumId, AgendaCommAddendumView, AgendaCommAddendumView> {

    @Override
    public SpotCheckObservation<CommitteeAgendaAddendumId> check(AgendaCommAddendumView content) throws ReferenceDataNotFoundEx {
        throw new NotImplementedException("");
    }

    @Override
    public SpotCheckObservation<CommitteeAgendaAddendumId> check(AgendaCommAddendumView content, LocalDateTime start, LocalDateTime end) throws ReferenceDataNotFoundEx {
        throw new NotImplementedException("");
    }

    @Override
    public SpotCheckObservation<CommitteeAgendaAddendumId>  check(AgendaCommAddendumView content, AgendaCommAddendumView reference) {
        final SpotCheckObservation<CommitteeAgendaAddendumId> observation = new SpotCheckObservation<>(reference.getCommitteeAgendaAddendumId());
        checkModifiedDateTime(content,reference,observation);
        checkChair(content,reference,observation);
        checkLocation(content,reference,observation);
        checkMeetingDateTime(content,reference,observation);
        checkNotes(content,reference,observation);
        checkBillListing(content,reference,observation);
        checkHasVotes(content,reference,observation);
        if(content.isHasVotes() && reference.isHasVotes()) {
            checkAttendanceList(content,reference,observation);
            checkVotesList(content,reference,observation);
        }
        return observation;
    }

    protected void checkModifiedDateTime(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation ) {
        checkString(content.getModifiedDateTime().toString(), reference.getModifiedDateTime().toString(),observation, SpotCheckMismatchType.AGENDA_MODIFIED_DATE_TIME);
    }

    protected void checkHasVotes(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(String.valueOf(content.isHasVotes()), String.valueOf(reference.isHasVotes()), observation, SpotCheckMismatchType.AGENDA_HAS_VOTES);
    }

    protected void checkChair(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(content.getMeeting().getChair(), reference.getMeeting().getChair(), observation, SpotCheckMismatchType.AGENDA_CHAIR);
    }

    protected void checkLocation(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(content.getMeeting().getLocation(), content.getMeeting().getLocation(), observation, SpotCheckMismatchType.AGENDA_LOCATION);
    }

    protected void checkMeetingDateTime(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(content.getMeeting().getMeetingDateTime().toString(), reference.getMeeting().getMeetingDateTime().toString(),observation,SpotCheckMismatchType.AGENDA_MEETING_TIME);
    }

    protected void checkNotes(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(content.getMeeting().getNotes(), reference.getMeeting().getNotes(), observation, SpotCheckMismatchType.AGENDA_NOTES);
    }

    protected void checkBillListing(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(OutputUtils.toJson(content.getBills()),OutputUtils.toJson(reference.getBills()), observation, SpotCheckMismatchType.AGENDA_BILL_LISTING);
    }

    protected void checkAttendanceList(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(OutputUtils.toJson(content.getVoteInfo().getAttendanceList()),OutputUtils.toJson(reference.getVoteInfo().getAttendanceList()), observation, SpotCheckMismatchType.AGENDA_ATTENDANCE_LIST);
    }

    protected void checkVotesList(AgendaCommAddendumView content, AgendaCommAddendumView reference,SpotCheckObservation<CommitteeAgendaAddendumId> observation) {
        checkString(OutputUtils.toJson(content.getVoteInfo().getVotesList()),OutputUtils.toJson(reference.getVoteInfo().getVotesList()), observation, SpotCheckMismatchType.AGENDA_VOTES_LIST);
    }
}