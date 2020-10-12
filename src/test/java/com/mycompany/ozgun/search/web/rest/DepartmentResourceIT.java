package com.mycompany.ozgun.search.web.rest;

import com.mycompany.ozgun.search.JhipsterforsearchApp;
import com.mycompany.ozgun.search.domain.Department;
import com.mycompany.ozgun.search.domain.Location;
import com.mycompany.ozgun.search.domain.Employee;
import com.mycompany.ozgun.search.repository.DepartmentRepository;
import com.mycompany.ozgun.search.service.DepartmentService;
import com.mycompany.ozgun.search.service.dto.DepartmentCriteria;
import com.mycompany.ozgun.search.service.DepartmentQueryService;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link DepartmentResource} REST controller.
 */
@SpringBootTest(classes = JhipsterforsearchApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class DepartmentResourceIT {

    private static final String DEFAULT_DEPARTMENT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DEPARTMENT_NAME = "BBBBBBBBBB";

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentQueryService departmentQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDepartmentMockMvc;

    private Department department;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Department createEntity(EntityManager em) {
        Department department = new Department()
            .departmentName(DEFAULT_DEPARTMENT_NAME);
        return department;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Department createUpdatedEntity(EntityManager em) {
        Department department = new Department()
            .departmentName(UPDATED_DEPARTMENT_NAME);
        return department;
    }

    @BeforeEach
    public void initTest() {
        department = createEntity(em);
    }

    @Test
    @Transactional
    public void createDepartment() throws Exception {
        int databaseSizeBeforeCreate = departmentRepository.findAll().size();
        // Create the Department
        restDepartmentMockMvc.perform(post("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(department)))
            .andExpect(status().isCreated());

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll();
        assertThat(departmentList).hasSize(databaseSizeBeforeCreate + 1);
        Department testDepartment = departmentList.get(departmentList.size() - 1);
        assertThat(testDepartment.getDepartmentName()).isEqualTo(DEFAULT_DEPARTMENT_NAME);
    }

    @Test
    @Transactional
    public void createDepartmentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = departmentRepository.findAll().size();

        // Create the Department with an existing ID
        department.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDepartmentMockMvc.perform(post("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(department)))
            .andExpect(status().isBadRequest());

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll();
        assertThat(departmentList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkDepartmentNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = departmentRepository.findAll().size();
        // set the field null
        department.setDepartmentName(null);

        // Create the Department, which fails.


        restDepartmentMockMvc.perform(post("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(department)))
            .andExpect(status().isBadRequest());

        List<Department> departmentList = departmentRepository.findAll();
        assertThat(departmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDepartments() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList
        restDepartmentMockMvc.perform(get("/api/departments?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(department.getId().intValue())))
            .andExpect(jsonPath("$.[*].departmentName").value(hasItem(DEFAULT_DEPARTMENT_NAME)));
    }
    
    @Test
    @Transactional
    public void getDepartment() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get the department
        restDepartmentMockMvc.perform(get("/api/departments/{id}", department.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(department.getId().intValue()))
            .andExpect(jsonPath("$.departmentName").value(DEFAULT_DEPARTMENT_NAME));
    }


    @Test
    @Transactional
    public void getDepartmentsByIdFiltering() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        Long id = department.getId();

        defaultDepartmentShouldBeFound("id.equals=" + id);
        defaultDepartmentShouldNotBeFound("id.notEquals=" + id);

        defaultDepartmentShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultDepartmentShouldNotBeFound("id.greaterThan=" + id);

        defaultDepartmentShouldBeFound("id.lessThanOrEqual=" + id);
        defaultDepartmentShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllDepartmentsByDepartmentNameIsEqualToSomething() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList where departmentName equals to DEFAULT_DEPARTMENT_NAME
        defaultDepartmentShouldBeFound("departmentName.equals=" + DEFAULT_DEPARTMENT_NAME);

        // Get all the departmentList where departmentName equals to UPDATED_DEPARTMENT_NAME
        defaultDepartmentShouldNotBeFound("departmentName.equals=" + UPDATED_DEPARTMENT_NAME);
    }

    @Test
    @Transactional
    public void getAllDepartmentsByDepartmentNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList where departmentName not equals to DEFAULT_DEPARTMENT_NAME
        defaultDepartmentShouldNotBeFound("departmentName.notEquals=" + DEFAULT_DEPARTMENT_NAME);

        // Get all the departmentList where departmentName not equals to UPDATED_DEPARTMENT_NAME
        defaultDepartmentShouldBeFound("departmentName.notEquals=" + UPDATED_DEPARTMENT_NAME);
    }

    @Test
    @Transactional
    public void getAllDepartmentsByDepartmentNameIsInShouldWork() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList where departmentName in DEFAULT_DEPARTMENT_NAME or UPDATED_DEPARTMENT_NAME
        defaultDepartmentShouldBeFound("departmentName.in=" + DEFAULT_DEPARTMENT_NAME + "," + UPDATED_DEPARTMENT_NAME);

        // Get all the departmentList where departmentName equals to UPDATED_DEPARTMENT_NAME
        defaultDepartmentShouldNotBeFound("departmentName.in=" + UPDATED_DEPARTMENT_NAME);
    }

    @Test
    @Transactional
    public void getAllDepartmentsByDepartmentNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList where departmentName is not null
        defaultDepartmentShouldBeFound("departmentName.specified=true");

        // Get all the departmentList where departmentName is null
        defaultDepartmentShouldNotBeFound("departmentName.specified=false");
    }
                @Test
    @Transactional
    public void getAllDepartmentsByDepartmentNameContainsSomething() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList where departmentName contains DEFAULT_DEPARTMENT_NAME
        defaultDepartmentShouldBeFound("departmentName.contains=" + DEFAULT_DEPARTMENT_NAME);

        // Get all the departmentList where departmentName contains UPDATED_DEPARTMENT_NAME
        defaultDepartmentShouldNotBeFound("departmentName.contains=" + UPDATED_DEPARTMENT_NAME);
    }

    @Test
    @Transactional
    public void getAllDepartmentsByDepartmentNameNotContainsSomething() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);

        // Get all the departmentList where departmentName does not contain DEFAULT_DEPARTMENT_NAME
        defaultDepartmentShouldNotBeFound("departmentName.doesNotContain=" + DEFAULT_DEPARTMENT_NAME);

        // Get all the departmentList where departmentName does not contain UPDATED_DEPARTMENT_NAME
        defaultDepartmentShouldBeFound("departmentName.doesNotContain=" + UPDATED_DEPARTMENT_NAME);
    }


    @Test
    @Transactional
    public void getAllDepartmentsByLocationIsEqualToSomething() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);
        Location location = LocationResourceIT.createEntity(em);
        em.persist(location);
        em.flush();
        department.setLocation(location);
        departmentRepository.saveAndFlush(department);
        Long locationId = location.getId();

        // Get all the departmentList where location equals to locationId
        defaultDepartmentShouldBeFound("locationId.equals=" + locationId);

        // Get all the departmentList where location equals to locationId + 1
        defaultDepartmentShouldNotBeFound("locationId.equals=" + (locationId + 1));
    }


    @Test
    @Transactional
    public void getAllDepartmentsByEmployeeIsEqualToSomething() throws Exception {
        // Initialize the database
        departmentRepository.saveAndFlush(department);
        Employee employee = EmployeeResourceIT.createEntity(em);
        em.persist(employee);
        em.flush();
        department.addEmployee(employee);
        departmentRepository.saveAndFlush(department);
        Long employeeId = employee.getId();

        // Get all the departmentList where employee equals to employeeId
        defaultDepartmentShouldBeFound("employeeId.equals=" + employeeId);

        // Get all the departmentList where employee equals to employeeId + 1
        defaultDepartmentShouldNotBeFound("employeeId.equals=" + (employeeId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDepartmentShouldBeFound(String filter) throws Exception {
        restDepartmentMockMvc.perform(get("/api/departments?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(department.getId().intValue())))
            .andExpect(jsonPath("$.[*].departmentName").value(hasItem(DEFAULT_DEPARTMENT_NAME)));

        // Check, that the count call also returns 1
        restDepartmentMockMvc.perform(get("/api/departments/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDepartmentShouldNotBeFound(String filter) throws Exception {
        restDepartmentMockMvc.perform(get("/api/departments?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDepartmentMockMvc.perform(get("/api/departments/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingDepartment() throws Exception {
        // Get the department
        restDepartmentMockMvc.perform(get("/api/departments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDepartment() throws Exception {
        // Initialize the database
        departmentService.save(department);

        int databaseSizeBeforeUpdate = departmentRepository.findAll().size();

        // Update the department
        Department updatedDepartment = departmentRepository.findById(department.getId()).get();
        // Disconnect from session so that the updates on updatedDepartment are not directly saved in db
        em.detach(updatedDepartment);
        updatedDepartment
            .departmentName(UPDATED_DEPARTMENT_NAME);

        restDepartmentMockMvc.perform(put("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedDepartment)))
            .andExpect(status().isOk());

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll();
        assertThat(departmentList).hasSize(databaseSizeBeforeUpdate);
        Department testDepartment = departmentList.get(departmentList.size() - 1);
        assertThat(testDepartment.getDepartmentName()).isEqualTo(UPDATED_DEPARTMENT_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingDepartment() throws Exception {
        int databaseSizeBeforeUpdate = departmentRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepartmentMockMvc.perform(put("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(department)))
            .andExpect(status().isBadRequest());

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll();
        assertThat(departmentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteDepartment() throws Exception {
        // Initialize the database
        departmentService.save(department);

        int databaseSizeBeforeDelete = departmentRepository.findAll().size();

        // Delete the department
        restDepartmentMockMvc.perform(delete("/api/departments/{id}", department.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Department> departmentList = departmentRepository.findAll();
        assertThat(departmentList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
