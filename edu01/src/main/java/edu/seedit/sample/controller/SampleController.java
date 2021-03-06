package edu.seedit.sample.controller;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.seedit.common.util.SeedController;
import edu.seedit.sample.service.SampleService;
import edu.seedit.sample.vo.SampleVO;


@Controller
public class SampleController extends SeedController  {
	Logger logger = Logger.getLogger(this.getClass());
	
	@Resource(name="SampleService")
	private SampleService sampleService;
	
	
	@RequestMapping(value="/sample/logout.do")
	public ModelAndView sessionDel(HttpSession session) throws Exception {
		session.invalidate();
		return new ModelAndView("redirect:/login/login.do");
	}
	
	
	@RequestMapping(value="/login/login.do")
	public ModelAndView login() throws Exception {
		return new ModelAndView("/login/login");
	}
	
/*	@ResponseBody
	@RequestMapping(value="/sample/UserIdCheck.do")
	public int userIdChk(@RequestParam("user_id") String userId) throws Exception {		
		logger.debug("login 아이디 : " + userId);		
		
		logger.debug("아이디가 있는지 없는지 : " + count);
		return count;
	}
	*/
	@ResponseBody
	@RequestMapping(value="/sample/UserLoginCheck.do")
	public String userLoginChk(@RequestBody @RequestParam Map<String,Object> requestParam, HttpSession session) throws Exception {		
		logger.debug("login 정보 : " + requestParam);
		String msg;
		String userId = (String)requestParam.get("userId");
		String userPw = (String)requestParam.get("userPw");		
		int count = sampleService.getUserIdCheck(userId);
		logger.debug("userIDChk  : " + count + "건이 조회되었습니다...");
		Map<String, Object> map= sampleService.getUserLoginCheck(userId);
		logger.debug("userLoginChk Value : " + map);		
		if(count==0){
			msg = URLEncoder.encode( "등록된 아이디가 아닙니다." , "UTF-8");
		}else if(!(userId.equals(map.get("userId"))&&userPw.equals(map.get("userPw")))){
			msg = URLEncoder.encode( "비밀번호가 틀렸습니다." , "UTF-8");			
		}else{
			session.setAttribute("userId", userId);
			msg = "success";
		}		
		return msg;
	}
	
	@RequestMapping(value="/sample/openSampleList_pre.do")
	public ModelAndView openSample(@RequestParam Map<String,Object> requestParam) throws Exception {
		showParameter(requestParam);

		ModelAndView mv = new ModelAndView("/sample/sampleList_pre");

		List<SampleVO> list = sampleService.getSampleList(requestParam);
		mv.addObject("list", list);

		return mv;
	}


	@RequestMapping(value="/sample/openSampleList.do"/*, method = RequestMethod.POST*/)
	public ModelAndView openSampleListGET(@RequestParam Map<String,Object> requestParam,
			HttpSession session) throws Exception {
		showParameter(requestParam);
		ModelAndView mv = new ModelAndView("/sample/sampleList2");
		List<SampleVO> list = sampleService.getSampleList(requestParam);			
		mv.addObject("list", list);			
		return mv;
	}
	
	@RequestMapping(value="/sample/openSampleOne.do")
	public ModelAndView openSampleListOne(@RequestParam(value="userId", defaultValue="test!!") String userId) throws Exception {
		
		ModelAndView mv = new ModelAndView("/sample/modify");
		SampleVO vo = sampleService.getSamplelist2(userId);
		mv.addObject("vo", vo); 
		

		return mv;
	}	
	
/*	@ResponseBody
	@RequestMapping(value="/sample/openSampleList.do", method = RequestMethod.POST)
	public  List<BlackVO> openSampleListJSON() throws Exception {		

		return sampleService.getBlackList();
		 
	}*/
	
	@ResponseBody
	@RequestMapping(value="/sample/openSampleList.do", method = RequestMethod.POST)
	public  ModelAndView openSampleListJSON(@RequestParam Map<String, Object> requestParam) throws Exception {		
	    ModelAndView mv = new ModelAndView("jsonView");
	    System.out.println("Controller : " + requestParam.get("PAGE_INDEX") + ", " + requestParam.get("PAGE_ROW"));
	    List<Map<String,Object>> list = sampleService.getSelectBoardList(requestParam);
	    System.out.println(list);
	    mv.addObject("list", list);
	    if(list.size() > 0){
	        mv.addObject("TOTAL", list.get(0).get("TOTAL_COUNT"));
	    }
	    else{
	        mv.addObject("TOTAL", 0);
	    }
	     
	    return mv;
		 
	}	

	@ResponseBody
	@RequestMapping(value="/sample/autoComplete.do", method = RequestMethod.POST)
	public  List<String> autoComplete(@RequestParam Map<String, Object> map) throws Exception {
		List<String> list = sampleService.getAutoComplete(map);	
		logger.debug("결과값:" + list);
		return list;
	}
	
	@RequestMapping(value="/sample/deleteUsers.do", method = RequestMethod.POST)
	public String deleteUser(@RequestParam("box") List<String> arr) throws Exception {
		logger.debug("넘어 온값 확인 : " + arr);
		
		int count = sampleService.getDeleteUser(arr);	
		logger.debug("변경된 del_YN : " + count + "건이 변경 되었습니다.");
		return  "redirect:/sample/openSampleList.do";
	}
	
	@ResponseBody
	@RequestMapping(value="/sample/userRank.do", method = RequestMethod.POST)
	public List<SampleVO> userRank(@RequestParam Map<String,Object> requestParam) throws Exception {
		logger.debug("userRank: " + requestParam);
		List<SampleVO> list = sampleService.getUserRank(requestParam);
		return list;
	}
	
/*	@RequestMapping(value="/sample/sampleWrite.do")
	public ModelAndView writeSample(@RequestParam Map<String,Object> requestParam) throws Exception {
		showParameter(requestParam);

		ModelAndView mv = new ModelAndView("/sample/sampleWrite");

		if(getValue(requestParam, "mode") != null && getValue(requestParam, "mode").equals("result")) {

			logger.debug("result before");
			Object result = sampleService.addSample(requestParam);
			logger.debug("result[" + result + "]:" + result.toString());
			mv.addObject("result", result);
			mv.addObject("mode", "result");
			logger.debug("result after");
		}

		return mv;
	}*/
	
	@RequestMapping(value="/sample/sampleWrite.do")
	public String writeSample(@Valid SampleVO sample, BindingResult bindingResult) throws Exception {
		
		System.out.println("11111");
		logger.debug("sample : "+ sample);
		
		if(bindingResult.hasErrors()){
			logger.debug("bindingResult has error!");
			List<ObjectError> errors= bindingResult.getAllErrors();
			for(ObjectError error : errors){
				logger.debug("error : " + error.getDefaultMessage());				
			}			
			return "/sample/sampleWrite";
		}		
		return null;
		
	}

	@RequestMapping(value="/sample/sampleWriteResult.do")
	public ModelAndView writeSampleResult(@RequestParam Map<String,Object> requestParam) throws Exception {
		showParameter(requestParam);

		ModelAndView mv = new ModelAndView("/sample/sampleWriteResult");

		Object result = sampleService.addSample(requestParam);
		logger.debug("result[" + result + "]:" + result.toString());
		mv.addObject("result", result);

		return mv;
	}

	/**
	 * 입력 파라미터 값 확인 후 리턴한다.
	 * @return
	 */
	private String getValue(Map<String,Object> requestParam, Object key) {
		if (key == null || key.toString().length() == 0) return "";

		Object value = requestParam.get(key);
		if (value == null || value.toString().length() == 0) return "";

		logger.debug("param [" + key + "," + value + "]");
		return value.toString();
	}

}
