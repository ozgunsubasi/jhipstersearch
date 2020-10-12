package com.mycompany.ozgun.search.web.rest;

import com.mycompany.ozgun.search.JhipsterforsearchApp;
import com.mycompany.ozgun.search.domain.JobHistory;
import com.mycompany.ozgun.search.domain.Job;
import com.mycompany.ozgun.search.domain.Department;
import com.mycompany.ozgun.search.domain.Employee;
import com.mycompany.ozgun.search.repository.JobHistoryRepository;
import com.mycompany.ozgun.search.service.JobHistoryService;
import com.mycompany.ozgun.search.service.dto.JobHistoryCriteria;
import com.mycompany.ozgun.search.service.JobHistoryQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.ozgun.search.domain.enumeration.Language;
/**
 * Integration tests for the {@link JobHistoryResource} REST controller.
 */
@SpringBootTest(classes = JhipsterforsearchApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class JobHistoryResourceIT {

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Language DEFAULT_LANGUAGE = Language.FRENCH;
    private static final Language UPDATED_LANGUAGE = Language.ENGLISH;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    @Autowired
    private JobHistoryService jobHistoryService;

    @Autowired
    private JobHistoryQueryService jobHistoryQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restJobHistoryMockMvc;

    private JobHistory jobHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JobHistory createEntity(EntityManager em) {
        JobHistory jobHistory = new JobHistory()
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .language(DEFAULT_LANGUAGE);
        return jobHistory;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JobHistory createUpdatedEntity(EntityManager em) {
        JobHistory jobHistory = new JobHistory()
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .language(UPDATED_LANGUAGE);
        return jobHistory;
    }

    @BeforeEach
    public void initTest() {
        jobHistory = createEntity(em);
    }

    @Test
    @Transactional
    public void createJobHistory() throws Exception {
        int databaseSizeBeforeCreate = jobHistoryRepository.findAll().size();
        // Create the JobHistory
        restJobHistoryMockMvc.perform(post("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(jobHistory)))
            .andExpect(status().isCreated());

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeCreate + 1);
        JobHistory testJobHistory = jobHistoryList.get(jobHistoryList.size() - 1);
        assertThat(testJobHistory.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testJobHistory.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testJobHistory.getLanguage()).isEqualTo(DEFAULT_LANGUAGE);
    }

    @Test
    @Transactional
    public void createJobHistoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = jobHistoryRepository.findAll().size();

        // Create the JobHistory with an existing ID
        jobHistory.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restJobHistoryMockMvc.perform(post("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(jobHistory)))
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllJobHistories() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList
        restJobHistoryMockMvc.perform(get("/api/job-histories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(jobHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString())));
    }
    
    @Test
    @Transactional
    public void getJobHistory() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get the jobHistory
        restJobHistoryMockMvc.perform(get("/api/job-histories/{id}", jobHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(jobHistory.getId().intValue()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.language").value(DEFAULT_LANGUAGE.toString()));
    }


    @Test
    @Transactional
    public void getJobHistoriesByIdFiltering() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        Long id = jobHistory.getId();

        defaultJobHistoryShouldBeFound("id.equals=" + id);
        defaultJobHistoryShouldNotBeFound("id.notEquals=" + id);

        defaultJobHistoryShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultJobHistoryShouldNotBeFound("id.greaterThan=" + id);

        defaultJobHistoryShouldBeFound("id.lessThanOrEqual=" + id);
        defaultJobHistoryShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllJobHistoriesByStartDateIsEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where startDate equals to DEFAULT_START_DATE
        defaultJobHistoryShouldBeFound("startDate.equals=" + DEFAULT_START_DATE);

        // Get all the jobHistoryList where startDate equals to UPDATED_START_DATE
        defaultJobHistoryShouldNotBeFound("startDate.equals=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByStartDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where startDate not equals to DEFAULT_START_DATE
        defaultJobHistoryShouldNotBeFound("startDate.notEquals=" + DEFAULT_START_DATE);

        // Get all the jobHistoryList where startDate not equals to UPDATED_START_DATE
        defaultJobHistoryShouldBeFound("startDate.notEquals=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByStartDateIsInShouldWork() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where startDate in DEFAULT_START_DATE or UPDATED_START_DATE
        defaultJobHistoryShouldBeFound("startDate.in=" + DEFAULT_START_DATE + "," + UPDATED_START_DATE);

        // Get all the jobHistoryList where startDate equals to UPDATED_START_DATE
        defaultJobHistoryShouldNotBeFound("startDate.in=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByStartDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where startDate is not null
        defaultJobHistoryShouldBeFound("startDate.specified=true");

        // Get all the jobHistoryList where startDate is null
        defaultJobHistoryShouldNotBeFound("startDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByEndDateIsEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where endDate equals to DEFAULT_END_DATE
        defaultJobHistoryShouldBeFound("endDate.equals=" + DEFAULT_END_DATE);

        // Get all the jobHistoryList where endDate equals to UPDATED_END_DATE
        defaultJobHistoryShouldNotBeFound("endDate.equals=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByEndDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where endDate not equals to DEFAULT_END_DATE
        defaultJobHistoryShouldNotBeFound("endDate.notEquals=" + DEFAULT_END_DATE);

        // Get all the jobHistoryList where endDate not equals to UPDATED_END_DATE
        defaultJobHistoryShouldBeFound("endDate.notEquals=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByEndDateIsInShouldWork() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where endDate in DEFAULT_END_DATE or UPDATED_END_DATE
        defaultJobHistoryShouldBeFound("endDate.in=" + DEFAULT_END_DATE + "," + UPDATED_END_DATE);

        // Get all the jobHistoryList where endDate equals to UPDATED_END_DATE
        defaultJobHistoryShouldNotBeFound("endDate.in=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByEndDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where endDate is not null
        defaultJobHistoryShouldBeFound("endDate.specified=true");

        // Get all the jobHistoryList where endDate is null
        defaultJobHistoryShouldNotBeFound("endDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByLanguageIsEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where language equals to DEFAULT_LANGUAGE
        defaultJobHistoryShouldBeFound("language.equals=" + DEFAULT_LANGUAGE);

        // Get all the jobHistoryList where language equals to UPDATED_LANGUAGE
        defaultJobHistoryShouldNotBeFound("language.equals=" + UPDATED_LANGUAGE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByLanguageIsNotEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where language not equals to DEFAULT_LANGUAGE
        defaultJobHistoryShouldNotBeFound("language.notEquals=" + DEFAULT_LANGUAGE);

        // Get all the jobHistoryList where language not equals to UPDATED_LANGUAGE
        defaultJobHistoryShouldBeFound("language.notEquals=" + UPDATED_LANGUAGE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByLanguageIsInShouldWork() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where language in DEFAULT_LANGUAGE or UPDATED_LANGUAGE
        defaultJobHistoryShouldBeFound("language.in=" + DEFAULT_LANGUAGE + "," + UPDATED_LANGUAGE);

        // Get all the jobHistoryList where language equals to UPDATED_LANGUAGE
        defaultJobHistoryShouldNotBeFound("language.in=" + UPDATED_LANGUAGE);
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByLanguageIsNullOrNotNull() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);

        // Get all the jobHistoryList where language is not null
        defaultJobHistoryShouldBeFound("language.specified=true");

        // Get all the jobHistoryList where language is null
        defaultJobHistoryShouldNotBeFound("language.specified=false");
    }

    @Test
    @Transactional
    public void getAllJobHistoriesByJobIsEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);
        Job job = JobResourceIT.createEntity(em);
        em.persist(job);
        em.flush();
        jobHistory.setJob(job);
        jobHistoryRepository.saveAndFlush(jobHistory);
        Long jobId = job.getId();

        // Get all the jobHistoryList where job equals to jobId
        defaultJobHistoryShouldBeFound("jobId.equals=" + jobId);

        // Get all the jobHistoryList where job equals to jobId + 1
        defaultJobHistoryShouldNotBeFound("jobId.equals=" + (jobId + 1));
    }


    @Test
    @Transactional
    public void getAllJobHistoriesByDepartmentIsEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);
        Department department = DepartmentResourceIT.createEntity(em);
        em.persist(department);
        em.flush();
        jobHistory.setDepartment(department);
        jobHistoryRepository.saveAndFlush(jobHistory);
        Long departmentId = department.getId();

        // Get all the jobHistoryList where department equals to departmentId
        defaultJobHistoryShouldBeFound("departmentId.equals=" + departmentId);

        // Get all the jobHistoryList where department equals to departmentId + 1
        defaultJobHistoryShouldNotBeFound("departmentId.equals=" + (departmentId + 1));
    }


    @Test
    @Transactional
    public void getAllJobHistoriesByEmployeeIsEqualToSomething() throws Exception {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory);
        Employee employee = EmployeeResourceIT.createEntity(em);
        em.persist(employee);
        em.flush();
        jobHistory.setEmployee(employee);
        jobHistoryRepository.saveAndFlush(jobHistory);
        Long employeeId = employee.getId();

        // Get all the jobHistoryList where employee equals to employeeId
        defaultJobHistoryShouldBeFound("employeeId.equals=" + employeeId);

        // Get all the jobHistoryList where employee equals to employeeId + 1
        defaultJobHistoryShouldNotBeFound("employeeId.equals=" + (employeeId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultJobHistoryShouldBeFound(String filter) throws Exception {
        restJobHistoryMockMvc.perform(get("/api/job-histories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(jobHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString())));

        // Check, that the count call also returns 1
        restJobHistoryMockMvc.perform(get("/api/job-histories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultJobHistoryShouldNotBeFound(String filter) throws Exception {
        restJobHistoryMockMvc.perform(get("/api/job-histories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restJobHistoryMockMvc.perform(get("/api/job-histories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingJobHistory() throws Exception {
        // Get the jobHistory
        restJobHistoryMockMvc.perform(get("/api/job-histories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateJobHistory() throws Exception {
        // Initialize the database
        jobHistoryService.save(jobHistory);

        int databaseSizeBeforeUpdate = jobHistoryRepository.findAll().size();

        // Update the jobHistory
        JobHistory updatedJobHistory = jobHistoryRepository.findById(jobHistory.getId()).get();
        // Disconnect from session so that the updates on updatedJobHistory are not directly saved in db
        em.detach(updatedJobHistory);
        updatedJobHistory
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .language(UPDATED_LANGUAGE);

        restJobHistoryMockMvc.perform(put("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedJobHistory)))
            .andExpect(status().isOk());

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeUpdate);
        JobHistory testJobHistory = jobHistoryList.get(jobHistoryList.size() - 1);
        assertThat(testJobHistory.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testJobHistory.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testJobHistory.getLanguage()).isEqualTo(UPDATED_LANGUAGE);
    }

    @Test
    @Transactional
    public void updateNonExistingJobHistory() throws Exception {
        int databaseSizeBeforeUpdate = jobHistoryRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc.perform(put("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(jobHistory)))
            .andExpect(status().isBadRequest());

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteJobHistory() throws Exception {
        // Initialize the database
        jobHistoryService.save(jobHistory);

        int databaseSizeBeforeDelete = jobHistoryRepository.findAll().size();

        // Delete the jobHistory
        restJobHistoryMockMvc.perform(delete("/api/job-histories/{id}", jobHistory.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
