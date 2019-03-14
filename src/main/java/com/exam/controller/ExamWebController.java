package com.exam.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.exam.model.Examination;
import com.exam.model.Grade;
import com.exam.model.Question;
import com.exam.model.User;
import com.exam.service.ExaminationService;
import com.exam.service.GradeService;
import com.exam.util.PageUtil;
import com.exam.util.ResultUtil;
import com.exam.vo.ExaminationConditionVo;
import com.exam.vo.base.ResponseVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Controller
public class ExamWebController {
	
	@Autowired
	private ExaminationService examService;
	@Autowired
	private GradeService gradeService;
	
	@RequestMapping("/")
	public String index(Model model) {
		if(SecurityUtils.getSubject().isAuthenticated()) {
			return "index/index";
		}else {
			return "index/index";
		}
	}
	
	@GetMapping("/exam")
	public String exam(Model model) {
		if(SecurityUtils.getSubject().isAuthenticated()) {
			return "redirect:/exam/examination";
		}else {
			return "redirect:/exam/login";
		}
	}	
	
	@GetMapping("/exam/examination")
	public String toExam(Model model, ExaminationConditionVo examConditionVo) {
		PageHelper.startPage(PageUtil.getPageNo(10, 0),10);
		List<Examination> examList = examService.findByCondition(examConditionVo);
		PageInfo<Examination> pages = new PageInfo<>(examList);
		model.addAttribute("pageInfo", pages);
		return "index/examination";
	}
	
	@GetMapping("/exam/login")
	public String login(Model model) {
		if(SecurityUtils.getSubject().isAuthenticated()) {
			return "redirect:/";
		}else {
			return "index/login";
		}
	}
	
	@GetMapping("/exam/startexam")
	public String startToExam(Model model, Integer id) {
		User user = (User)SecurityUtils.getSubject().getPrincipal();
		List<Examination> listExam = examService.queryByExamId(id);
		Map<String, Object> data = new HashMap<>();
		data.put("exam", listExam);
		model.addAttribute("data", data);
		model.addAttribute("user", user);
		return "index/detail";
	}
	
	/**
	 * 提交考试
	 * @param grade
	 * @return
	 */
	@PostMapping("/exam/submitExam")
	@ResponseBody
	public ResponseVo submitExam(@RequestBody Grade grade) {
		try {
			User user = (User)SecurityUtils.getSubject().getPrincipal();
			List<String> answerStr = Arrays.asList(grade.getAnswerJson().split(","));
			int autoResult = 0;
			List<Examination> listExam = examService.queryByExamId(grade.getExamId());
			List<Question> questions = listExam.get(0).getQuestions();
			for(int i = 0; i < questions.size(); i++) {
				Question question = questions.get(i);
				if(question.getQuestionType() <= 1 && question.getAnswer().equals(answerStr.get(i))) {
					autoResult += question.getScore();
				}
			}
			grade.setUserId(user.getUserId());
			grade.setAutoResult(autoResult);
			grade.setResult(autoResult);
			grade.setManulResult(0);
			grade.setStatus(0);
			gradeService.insertSelective(grade);
			return ResultUtil.success("提交考试成功！");
		} catch (Exception e) {
			return ResultUtil.error("提交考试失败！请联系管理员处理");
		}
	}
	
}
