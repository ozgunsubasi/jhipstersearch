import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'region',
        loadChildren: () => import('./region/region.module').then(m => m.JhipsterforsearchRegionModule),
      },
      {
        path: 'country',
        loadChildren: () => import('./country/country.module').then(m => m.JhipsterforsearchCountryModule),
      },
      {
        path: 'location',
        loadChildren: () => import('./location/location.module').then(m => m.JhipsterforsearchLocationModule),
      },
      {
        path: 'department',
        loadChildren: () => import('./department/department.module').then(m => m.JhipsterforsearchDepartmentModule),
      },
      {
        path: 'task',
        loadChildren: () => import('./task/task.module').then(m => m.JhipsterforsearchTaskModule),
      },
      {
        path: 'employee',
        loadChildren: () => import('./employee/employee.module').then(m => m.JhipsterforsearchEmployeeModule),
      },
      {
        path: 'job',
        loadChildren: () => import('./job/job.module').then(m => m.JhipsterforsearchJobModule),
      },
      {
        path: 'job-history',
        loadChildren: () => import('./job-history/job-history.module').then(m => m.JhipsterforsearchJobHistoryModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class JhipsterforsearchEntityModule {}
