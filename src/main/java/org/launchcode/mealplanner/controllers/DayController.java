package org.launchcode.mealplanner.controllers;


import org.launchcode.mealplanner.models.Day;
import org.launchcode.mealplanner.models.Meal;
import org.launchcode.mealplanner.models.data.DayDao;
import org.launchcode.mealplanner.models.forms.BuildDayForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("day")
public class DayController extends AbstractController{


    @RequestMapping(value = "")
    public String index (Model model) {

        model.addAttribute("title", "Day Planner");
//        model.addAttribute("days", dayDao.findAll());
        model.addAttribute(new Day());

        return "day/index";
    }

/*    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String displayCreateDayForm(Model model) {

        model.addAttribute("title", "Create New Day");
        model.addAttribute(new Day());

        return "day/create";
    }*/

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String createDay(@ModelAttribute @Valid Day newDay, Errors errors, Model model, HttpServletRequest request) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Day Planner");
            return "day/index";
        }

        List<Day> userDays = new ArrayList<>();
        for(Day day : dayDao.findAll()) {
            if(day.getUser() == getUserFromSession(request.getSession())) {
                userDays.add(day);
            }
        }
//        Day existingDay = dayDao.findByName(newDay.getName());

        for(Day existingDay : userDays) {
            if(existingDay.getName().equals(newDay.getName())) {
                return"redirect:/day/build/" + existingDay.getId();
            }
        }
/*
        if(existingDay != null) {
            if(existingDay.getUser() == getUserFromSession(request.getSession())) {
                return"redirect:/day/build/" + existingDay.getId();
            }

        }
*/

        newDay.setUser(getUserFromSession(request.getSession()));
        dayDao.save(newDay);



        return "redirect:/day/build/" + newDay.getId();
    }

    @RequestMapping(value = "build/{id}", method = RequestMethod.GET)
    public String displayBuildDayForm (Model model, @PathVariable (value = "id") int id) {
        BuildDayForm buildDayForm = new BuildDayForm(dayDao.findById(id).orElse(null), mealDao.findAll());

        model.addAttribute(new Day());
        model.addAttribute("form", buildDayForm);
        model.addAttribute("title", "Plan Meals for Day: " + dayDao.findById(id).orElse(null).getName());

        return "day/build";
    }

    @RequestMapping(value = "build/{id}", method = RequestMethod.POST)
    public String processBuildDayForm (Model model, @ModelAttribute @ Valid BuildDayForm form, Errors errors) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Plan Day: " + dayDao.findById(form.getDayId()).orElse(null).getName());

            return "day/build";
        }

/*        Day existingDay = dayDao.findByName(newDay.getName());

        if(existingDay != null) {
            return"redirect:/day/build/" + existingDay.getId();
        }*/

        Meal newMeal = mealDao.findById(form.getMealId()).orElse(null);
        Day currentDay = dayDao.findById(form.getDayId()).orElse(null);

        currentDay.addMeal(newMeal);
        currentDay.calculateTotals();

        dayDao.save(currentDay);

        return "redirect:/day/build/" + currentDay.getId();
    }

    @RequestMapping(value = "build/remove-meal/{id}", method = RequestMethod.POST)
    public String removeComponentFromMeal(Model model, @PathVariable(value="id") int id, @ModelAttribute @Valid BuildDayForm form) {


        Meal discardedMeal = mealDao.findById(id).orElse(null);
        Day currentDay = dayDao.findById(form.getDayId()).orElse(null);

        currentDay.removeMeal(discardedMeal);
        currentDay.calculateTotals();
        dayDao.save(currentDay);

        return "redirect:/day/build/" + currentDay.getId();
    }

    @RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
    public String deleteDay( Model model, @PathVariable(value ="id") int id) {

        model.addAttribute("day", dayDao.findById(id).orElse(null));

        dayDao.deleteById(id);

        return "day/delete";
    }


}
