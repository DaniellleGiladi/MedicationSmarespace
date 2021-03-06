package smartspace.plugins;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.dao.rdb.ActionCrud;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;

@Component
public class GetAllCheckInPlugin implements Plugin {
	private ActionCrud actionCrud;
	private AdvancedUserDao<String> userDao;
	private UpdateProperties up;
	private AdvancedElementDao<String> elementDao;

	@Autowired
	public GetAllCheckInPlugin(ActionCrud actionCrud, AdvancedUserDao<String> dao,UpdateProperties up , AdvancedElementDao<String> elementDao) {
		super();
		this.actionCrud = actionCrud;
		this.userDao = dao;
		this.up = up;
		this.elementDao = elementDao;

	}

	@Override
	public Object doSomething(ActionEntity action) throws Exception {
		UserEntity user = this.userDao.readById(action.getPlayerSmartspace() + "#" + action.getPlayerEmail()).get();
		this.userDao.updatePoints(user, 10L);
		Map<String, Object> map = action.getMoreAttributes();
		GetAllCheckInput input = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(map),
				GetAllCheckInput.class);

		List<ActionEntity> actions = this.actionCrud.findAllByActionTypeAndElementSmartspaceAndElementId("CheckIn",
				action.getElementSmartspace(), action.getElementId(), PageRequest.of(input.getPage(), input.getSize()));

		List<UserCheckAction> listOfNames = actions.stream()
				.map(i -> new UserCheckAction(i.getPlayerEmail(), i.getPlayerSmartspace(),i.getCreationTimestamp()))
				.collect(Collectors.toList());
		ElementEntity element = this.elementDao.readById(action.getElementSmartspace()+"#"+action.getElementId()).get();
		up.addHistory(element , action.getActionType(), action.getCreationTimestamp().toString());

		
		return new GetAllCheckResponse(listOfNames);
	}

}
