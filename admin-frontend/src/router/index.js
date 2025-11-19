import Vue from 'vue'
import Router from 'vue-router'
import Attendance from '../pages/Attendance.vue'
import HomeDashboard from '../pages/HomeDashboard.vue'
const ScheduleView = () => import('../pages/course-class-center/ScheduleView.vue')
const AttendanceStat = () => import('../pages/AttendanceStat.vue')
const ReportEnroll = () => import('../pages/report/ReportEnroll.vue')
const ReportTeaching = () => import('../pages/report/ReportTeaching.vue')
const ReportFinance = () => import('../pages/report/ReportFinance.vue')
const ReportAudition = () => import('../pages/report/ReportAudition.vue')
const Campus = () => import('../pages/base-platform/Campus.vue')
const Dictionary = () => import('../pages/base-platform/Dictionary.vue')
const OrgStaff = () => import('../pages/base-platform/OrgStaff.vue')
const Dept = () => import('../pages/base-platform/Dept.vue')
const RolePerm = () => import('../pages/base-platform/RolePerm.vue')
const Teacher = () => import('../pages/base-platform/Teacher.vue')
const FeeRuleConfig = () => import('../pages/teacher-salary/FeeRuleConfig.vue')
const FeeTrial = () => import('../pages/teacher-salary/FeeTrial.vue')
const TeacherInfoManage = () => import('../pages/teacher-salary/TeacherInfoManage.vue')
const HolidayManage = () => import('../pages/base-platform/HolidayManage.vue')
const SubjectDict = () => import('../pages/base-platform/SubjectDict.vue')
const ClassManageBase = () => import('../pages/base-platform/ClassManage.vue')
const GradeDict = () => import('../pages/base-platform/GradeDict.vue')
const ClassroomManage = () => import('../pages/base-platform/Classroom.vue')
const PermCatalog = () => import('../pages/base-platform/PermCatalog.vue')
const BasePlaceholder = () => import('../pages/base-platform/BasePlaceholder.vue')
const StudentProfile = () => import('../pages/student-center/StudentProfile.vue')
const AuditionManage = () => import('../pages/student-center/AuditionManage.vue')
const EnrollmentManage = () => import('../pages/student-center/EnrollmentManage.vue')
const GuardianPerm = () => import('../pages/student-center/GuardianPerm.vue')
const ReferralManage = () => import('../pages/student-center/ReferralManage.vue')
const StudentChangeManage = () => import('../pages/student-center/StudentChangeManage.vue')
const StudentManage = () => import('../pages/student-center/StudentManage.vue')
const StudentClassSelect = () => import('../pages/student-center/StudentClassSelect.vue')
const GradeManage = () => import('../pages/student-center/GradeManage.vue')
const CourseManage = () => import('../pages/course-class-center/CourseManage.vue')
const TextbookManage = () => import('../pages/course-class-center/TextbookManage.vue')
// 已移除：课程与班级中心的班级列表与班级管理
const ScheduleManage = () => import('../pages/course-class-center/ScheduleManage.vue')
const Login = () => import('../pages/Login.vue')
const FeeManage = () => import('../pages/finance-settlement/FeeManage.vue')
const RefundManage = () => import('../pages/finance-settlement/RefundManage.vue')
const RefundApproverConfig = () => import('../pages/finance-settlement/RefundApproverConfig.vue')
const RenewManage = () => import('../pages/finance-settlement/RenewManage.vue')
// 发票管理
const InvoiceApply = () => import('../pages/finance-invoice/InvoiceApply.vue')
const InvoiceManage = () => import('../pages/finance-invoice/InvoiceManage.vue')
const InvoiceStat = () => import('../pages/finance-invoice/InvoiceStat.vue')


// 统一处理导航重定向/重复导航导致的 Promise reject，避免控制台错误噪音
const originalPush = Router.prototype.push
Router.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) return originalPush.call(this, location, onResolve, onReject)
  return originalPush.call(this, location).catch(err => err)
}
const originalReplace = Router.prototype.replace
Router.prototype.replace = function replace(location, onResolve, onReject) {
  if (onResolve || onReject) return originalReplace.call(this, location, onResolve, onReject)
  return originalReplace.call(this, location).catch(err => err)
}

Vue.use(Router)

const router = new Router({
  mode: 'hash',
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', component: HomeDashboard },
    { path: '/report/enroll', component: ReportEnroll, meta: { requiredPerm: ['menu:report:enroll'] } },
    { path: '/report/teaching', component: ReportTeaching, meta: { requiredPerm: ['menu:report:teaching'] } },
    { path: '/report/finance', component: ReportFinance, meta: { requiredPerm: ['menu:report:finance'] } },
    { path: '/report/audition', component: ReportAudition, meta: { requiredPerm: ['menu:report:audition'] } },
    // 兼容旧入口：教师与课时费管理聚合页
    { path: '/teacher-salary', redirect: '/teacher-salary/rule' },
    // 移除顶级“教师管理”入口，仅保留基础平台教师管理
    { path: '/login', component: Login },
    { path: '/attendance', component: Attendance },
    { path: '/attendance/stat', component: AttendanceStat, meta: { requiredPerm: 'attendance:stat' } },
    // 顶级课表查询路由已移除：统一保留课程中心下的课表查询
    { path: '/base/campus', component: Campus, meta: { requiredPerm: 'menu:base:campus' } },
    { path: '/base/dict', component: Dictionary, meta: { requiredPerm: 'menu:base:dict' } },
    { path: '/base/dept', component: Dept, meta: { requiredPerm: 'menu:base:dept' } },
    { path: '/base/role', component: RolePerm, meta: { requiredPerm: 'menu:base:role' } },
    { path: '/base/user', component: OrgStaff, meta: { requiredPerm: 'menu:base:user' } },
    { path: '/base/teacher', component: Teacher, meta: { requiredPerm: 'menu:base:teacher' } },
    { path: '/base/subject-dict', component: SubjectDict, meta: { requiredPerm: 'menu:base:subject-dict' } },
    { path: '/base/class-dict', component: ClassManageBase, meta: { requiredPerm: 'menu:base:class-dict' } },
    { path: '/base/grade-dict', component: GradeDict, meta: { requiredPerm: 'menu:base:grade-dict' } },
    { path: '/base/classroom', component: ClassroomManage, meta: { requiredPerm: 'menu:base:classroom' } },
    { path: '/base/perm', component: PermCatalog, meta: { requiredPerm: 'menu:base:perm' } },
    { path: '/base/holiday', component: HolidayManage, meta: { requiredPerm: 'menu:base:holiday' } },
    { path: '/base/:code', component: BasePlaceholder }
    ,{ path: '/student/profile', component: StudentProfile, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/audition', component: AuditionManage, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/enroll', component: EnrollmentManage, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/guardian-perm', component: GuardianPerm, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/referral', component: ReferralManage, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/change', component: StudentChangeManage, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/manage', component: StudentManage, meta: { requiredPerm: 'student:view' } }
    ,{ path: '/student/class-select', component: StudentClassSelect, meta: { requiredPerm: 'class:select' } }
    ,{ path: '/student/grade', component: GradeManage, meta: { requiredPerm: 'menu:student:grade' } }
    ,{ path: '/course/manage', component: CourseManage, meta: { requiredPerm: 'course:view' } }
    ,{ path: '/course/textbook', component: TextbookManage, meta: { requiredPerm: 'course:view' } }
    // 移除班级列表与班级管理路由
  ,{ path: '/course/schedule', component: ScheduleManage, meta: { requiredPerm: ['course:schedule', 'menu:course:schedule'] } }
  ,{ path: '/course/schedule-view', component: ScheduleView, meta: { requiredPerm: ['course:schedule', 'menu:course:schedule'] } }
    // 财务管理下的课时费路由
    ,{ path: '/finance/teacher-fee/rule', component: FeeRuleConfig, meta: { requiredPerm: 'menu:finance:teacher-fee:rule' } }
    ,{ path: '/finance/teacher-fee/trial', component: FeeTrial, meta: { requiredPerm: 'menu:finance:teacher-fee:trial' } }
    // 旧 teacher-salary 路由保留以兼容现有权限与入口
    ,{ path: '/teacher-salary/rule', component: FeeRuleConfig, meta: { requiredPerm: 'menu:teacher-salary' } }
    ,{ path: '/teacher-salary/teacher', component: TeacherInfoManage, meta: { requiredPerm: 'menu:teacher-salary' } }
    ,{ path: '/teacher-salary/trial', component: FeeTrial, meta: { requiredPerm: 'menu:teacher-salary' } }
    // 财务管理：收费管理
    ,{ path: '/finance', redirect: '/finance/settlement/fee' }
    ,{ path: '/finance/settlement/fee', component: FeeManage, meta: { requiredPerm: 'menu:finance:settlement:fee' } }
    ,{ path: '/finance/settlement/renew', component: RenewManage, meta: { requiredPerm: 'menu:finance:settlement:renew' } }
    ,{ path: '/finance/settlement/refund', component: RefundManage, meta: { requiredPerm: 'menu:finance:settlement:refund' } }
    ,{ path: '/finance/settlement/approver-config', component: RefundApproverConfig, meta: { requiredPerm: 'menu:finance:settlement:approver-config' } }
    // 发票管理
    ,{ path: '/finance/invoice/apply', component: InvoiceApply, meta: { requiredPerm: 'menu:finance:invoice:apply' } }
  ,{ path: '/finance/invoice/manage', component: InvoiceManage, meta: { requiredPerm: 'menu:finance:invoice:manage' } }
  ,{ path: '/finance/invoice/stat', component: InvoiceStat, meta: { requiredPerm: 'menu:finance:invoice:stat' } }

  // 学员中心：报名页面
  ,{ path: '/student/enroll', component: () => import('../pages/student-center/StudentEnroll.vue') }

  ]
})

// 权限指令与路由守卫在 main.js 已安装，这里不重复安装

export default router