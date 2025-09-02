import React, { useEffect, useMemo, useState } from 'react';
import { Container, Card, Button, Table, Badge, Modal, Form } from 'react-bootstrap';
import { Formik } from 'formik';
import * as Yup from 'yup';
import { useToast } from '../../components/common/ToastNotification';
import SkeletonLoader from '../../components/common/SkeletonLoader';
import { ButtonSpinner } from '../../components/common/LoadingSpinner';
import ConfirmationModal, { ProcessConfirmationModal } from '../../components/common/ConfirmationModal';
import apiService, { payrollAPI } from '../../api/apiService';
import './styles/PayrollManagementPage.css';

const yearOptions = Array.from({ length: 7 }, (_, i) => new Date().getFullYear() + 1 - i);
const monthOptions = [
  { value: 1, label: 'January' },
  { value: 2, label: 'February' },
  { value: 3, label: 'March' },
  { value: 4, label: 'April' },
  { value: 5, label: 'May' },
  { value: 6, label: 'June' },
  { value: 7, label: 'July' },
  { value: 8, label: 'August' },
  { value: 9, label: 'September' },
  { value: 10, label: 'October' },
  { value: 11, 'label': 'November' },
  { value: 12, label: 'December' },
];

const createRunSchema = Yup.object().shape({
  year: Yup.number().required('Year is required').min(2000, 'Year is too early').max(2100, 'Year is too late'),
  month: Yup.number().required('Month is required').min(1).max(12),
});

const normalizeStatus = (status) => {
  if (!status) return '';
  const s = String(status).toLowerCase();
  if (s === 'draft') return 'DRAFT';
  if (s === 'processed') return 'PROCESSED';
  if (s === 'locked') return 'LOCKED';
  return String(status).toUpperCase();
};

const statusBadge = (status) => {
  const key = normalizeStatus(status);
  const map = {
    DRAFT: { bg: 'secondary', text: 'Draft' },
    PROCESSED: { bg: 'info', text: 'Processed' },
    LOCKED: { bg: 'success', text: 'Locked' },
  };
  const cfg = map[key] || { bg: 'dark', text: status };
  return <Badge bg={cfg.bg} className="status-badge">{cfg.text}</Badge>;
};

const formatDateTime = (val) => {
  if (!val) return '-';
  try { return new Date(val).toLocaleString(); } catch { return '-'; }
};

const formatCurrency = (amount) => {
  const n = Number(amount || 0);
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);
};

const resolveMonthlyBasePay = (item) => {
  const candidates = [
    item.monthlyBaseSalary,
    item.monthlyBasePay,
    item.basePay,
    item.baseSalary,
  ];
  const value = candidates.find((v) => typeof v === 'number' || (typeof v === 'string' && v !== ''));
  return Number(value || 0);
};

const resolveBonus = (item) => {
  const candidates = [item.totalBonus, item.bonusAmount, item.bonus, item.allowances];
  const value = candidates.find((v) => typeof v === 'number' || (typeof v === 'string' && v !== ''));
  return Number(value || 0);
};

const resolveDeductions = (item) => {
  const candidates = [item.totalDeductions, item.deductionsAmount, item.deductions];
  const value = candidates.find((v) => typeof v === 'number' || (typeof v === 'string' && v !== ''));
  return Number(value || 0);
};

const resolveNet = (item) => {
  const candidates = [item.netSalary, item.netPay, item.net];
  const value = candidates.find((v) => typeof v === 'number' || (typeof v === 'string' && v !== ''));
  return Number(value || 0);
};

const PayrollManagementPage = () => {
  const { showToast } = useToast();

  const [runs, setRuns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);

  const [processingRunId, setProcessingRunId] = useState(null);
  const [lockingRun, setLockingRun] = useState(null); // { id, show }
  const [lockingInProgress, setLockingInProgress] = useState(false);

  const [itemsRunId, setItemsRunId] = useState(null);
  const [itemsLoading, setItemsLoading] = useState(false);
  const [runItems, setRunItems] = useState([]);

  useEffect(() => {
    fetchRuns();
  }, []);

  const fetchRuns = async () => {
    try {
      setLoading(true);
      const timestamp = Date.now();
      const res = await apiService.get('/payroll/runs', {
        params: { _t: timestamp },
        headers: {
          'Cache-Control': 'no-cache',
          'Pragma': 'no-cache'
        }
      });
      const data = Array.isArray(res.data?.data) ? res.data.data : [];
      data.sort((a, b) => {
        const ay = a.runYear ?? a.year ?? 0;
        const by = b.runYear ?? b.year ?? 0;
        const am = a.runMonth ?? a.month ?? 0;
        const bm = b.runMonth ?? b.month ?? 0;
        if (by !== ay) return by - ay;
        if (bm !== am) return bm - am;
        const at = new Date(a.createdAt || 0).getTime();
        const bt = new Date(b.createdAt || 0).getTime();
        return bt - at;
      });
      setRuns(data);
    } catch (e) {
      console.error('Error fetching runs:', e);
      showToast(e.message || 'Failed to load payroll runs', 'error');
    } finally {
      setLoading(false);
    }
  };

  const openItemsModal = async (runId) => {
    setItemsRunId(runId);
    setRunItems([]);
    setItemsLoading(true);
    try {
      const res = await apiService.get(`/payroll/runs/${runId}/items`, { params: { _ts: Date.now() } });
      setRunItems(res.data.data || []);
    } catch (e) {
      showToast(e.message || 'Failed to load run items', 'error');
    } finally {
      setItemsLoading(false);
    }
  };

  const handleCreateRun = async (values, { setSubmitting, resetForm }) => {
    try {
      setCreating(true);
      const response = await payrollAPI.createRun({ year: values.year, month: values.month });
      const newRun = response.data?.data;

      // --- FIX START ---
      // Manually add the new run to the state for an instant UI update.
      if (newRun) {
        setRuns(prevRuns => {
          const updatedRuns = [newRun, ...prevRuns];
          updatedRuns.sort((a, b) => {
            const ay = a.runYear ?? a.year ?? 0;
            const by = b.runYear ?? b.year ?? 0;
            const am = a.runMonth ?? a.month ?? 0;
            const bm = b.runMonth ?? b.month ?? 0;
            if (by !== ay) return by - ay;
            if (bm !== am) return bm - am;
            const at = new Date(a.createdAt || 0).getTime();
            const bt = new Date(b.createdAt || 0).getTime();
            return bt - at;
          });
          return updatedRuns;
        });
      } else {
        // Fallback to refetch if the API doesn't return the new object
        await fetchRuns();
      }
      // --- FIX END ---

      setShowCreateModal(false);
      resetForm();
      showToast('Payroll run created', 'success');

    } catch (e) {
      console.error('Create run error:', e);
      showToast(e.message || 'Failed to create payroll run', 'error');
    } finally {
      setCreating(false);
      setSubmitting(false);
    }
  };

  const handleProcess = async (runId) => {
    try {
      setProcessingRunId(runId);
      const response = await payrollAPI.processRun(runId);
      const updatedRunData = response.data?.data;

      // --- FIX START ---
      // Optimistically update the run's status in the local state.
      setRuns(prevRuns =>
        prevRuns.map(run =>
          (run.runId || run.id) === runId
            ? { ...run, status: 'PROCESSED', processedAt: updatedRunData?.processedAt || new Date().toISOString() }
            : run
        )
      );
      // --- FIX END ---

      showToast('Payroll processed successfully', 'success');

      if (itemsRunId === runId) {
        await openItemsModal(runId);
      }
    } catch (e) {
      console.error('Process run error:', e);
      showToast(e.message || 'Failed to process payroll', 'error');
      await fetchRuns(); // Refetch on error to sync state
    } finally {
      setProcessingRunId(null);
    }
  };

  const handleLock = async () => {
    if (!lockingRun) return;
    const runIdToLock = lockingRun.id;

    try {
      setLockingInProgress(true);
      const response = await payrollAPI.lockRun(runIdToLock);
      const updatedRunData = response.data?.data;
      
      // --- FIX START ---
      // Optimistically update the run's status in the local state.
      setRuns(prevRuns =>
        prevRuns.map(run =>
          (run.runId || run.id) === runIdToLock
            ? { ...run, status: 'LOCKED', lockedAt: updatedRunData?.lockedAt || new Date().toISOString() }
            : run
        )
      );
      // --- FIX END ---

      showToast('Payroll run locked', 'success');

      if (itemsRunId === runIdToLock) {
        await openItemsModal(runIdToLock);
      }
    } catch (e) {
      console.error('Lock run error:', e);
      showToast(e.message || 'Failed to lock payroll run', 'error');
      await fetchRuns(); // Refetch on error to sync state
    } finally {
      setLockingInProgress(false);
      setLockingRun(null);
    }
  };

  const renderCreateRunModal = () => (
    <Modal show={showCreateModal} onHide={() => !creating && setShowCreateModal(false)} centered>
      <Modal.Header closeButton={!creating}>
        <Modal.Title>Create New Payroll Run</Modal.Title>
      </Modal.Header>
      <Formik
        initialValues={{ year: new Date().getFullYear(), month: new Date().getMonth() + 1 }}
        validationSchema={createRunSchema}
        onSubmit={handleCreateRun}
      >
        {({ values, errors, touched, handleChange, handleBlur, handleSubmit, isSubmitting }) => (
          <form onSubmit={handleSubmit}>
            <Modal.Body>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <Form.Group>
                    <Form.Label>Year</Form.Label>
                    <Form.Select
                      name="year"
                      value={values.year}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.year && !!errors.year}
                    >
                      {yearOptions.map((y) => (
                        <option key={y} value={y}>{y}</option>
                      ))}
                    </Form.Select>
                    <Form.Control.Feedback type="invalid">{errors.year}</Form.Control.Feedback>
                  </Form.Group>
                </div>
                <div className="col-md-6 mb-3">
                  <Form.Group>
                    <Form.Label>Month</Form.Label>
                    <Form.Select
                      name="month"
                      value={values.month}
                      onChange={handleChange}
                      onBlur={handleBlur}
      isInvalid={touched.month && !!errors.month}
                    >
                      {monthOptions.map((m) => (
                        <option key={m.value} value={m.value}>{m.label}</option>
                      ))}
                    </Form.Select>
                    <Form.Control.Feedback type="invalid">{errors.month}</Form.Control.Feedback>
                  </Form.Group>
                </div>
              </div>
              <div className="alert alert-info small mb-0">
                Ensure you are creating the run for the correct period. This defines which employees and salary structures are considered.
              </div>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="outline-secondary" onClick={() => setShowCreateModal(false)} disabled={creating || isSubmitting}>
                Cancel
              </Button>
              <Button type="submit" variant="primary" disabled={creating || isSubmitting}>
                {creating || isSubmitting ? <ButtonSpinner text="Creating" /> : 'Create Run'}
              </Button>
            </Modal.Footer>
          </form>
        )}
      </Formik>
    </Modal>
  );

  const renderRunsTable = () => {
    if (loading) {
      return (
        <div className="skeleton-table">
          <SkeletonLoader height="40px" className="mb-2" />
          {[...Array(6)].map((_, i) => (
            <SkeletonLoader key={i} height="56px" className="mb-2" />
          ))}
        </div>
      );
    }

    if (!runs.length) {
      return (
        <div className="no-runs text-center py-5">
          <i className="fas fa-file-invoice-dollar fa-3x text-muted mb-3"></i>
          <h6 className="text-muted">No payroll runs found</h6>
          <p className="text-muted mb-0">Create your first monthly payroll run to begin processing salaries.</p>
        </div>
      );
    }

    const monthLabel = (m) => (monthOptions.find((x) => x.value === m)?.label || m);

    return (
      <Table hover responsive className="runs-table">
        <thead>
          <tr>
            <th>Period</th>
            <th>Status</th>
            <th>Created At</th>
            <th>Processed At</th>
            <th>Locked At</th>
            <th style={{ width: 280 }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {runs.map((run) => (
            <tr key={run.runId || run.id}>
              <td>
                <div className="fw-semibold">{monthLabel(run.runMonth ?? run.month)} {run.runYear ?? run.year}</div>
                <small className="text-muted">Run ID: {run.runId || run.id}</small>
              </td>
              <td>{statusBadge(run.status)}</td>
              <td>{formatDateTime(run.createdAt)}</td>
              <td>{formatDateTime(run.processedAt)}</td>
              <td>{formatDateTime(run.lockedAt)}</td>
              <td>
                <div className="d-flex gap-2 flex-wrap">
                  {(normalizeStatus(run.status) === 'DRAFT') && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleProcess(run.runId || run.id)}
                      disabled={processingRunId === (run.runId || run.id)}
                    >
                      {processingRunId === (run.runId || run.id) ? <ButtonSpinner text="Processing" /> : (<><i className="fas fa-cogs me-1"></i> Process</>)}
                    </Button>
                  )}

                  {(normalizeStatus(run.status) === 'PROCESSED') && (
                    <>
                      <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => handleProcess(run.runId || run.id)}
                        disabled={processingRunId === (run.runId || run.id)}
                      >
                        {processingRunId === (run.runId || run.id) ? <ButtonSpinner text="Re-Processing" /> : (<><i className="fas fa-sync me-1"></i> Re-Process</>)}
                      </Button>
                      <Button
                        variant="outline-secondary"
                        size="sm"
                        onClick={() => openItemsModal(run.runId || run.id)}
                      >
                        <i className="fas fa-list me-1"></i> View Items
                      </Button>
                      <Button
                        variant="success"
                        size="sm"
                        onClick={() => setLockingRun({ id: run.runId || run.id, show: true })}
                      >
                        <i className="fas fa-lock me-1"></i> Lock
                      </Button>
                    </>
                  )}

                  {(normalizeStatus(run.status) === 'LOCKED') && (
                    <Button
                      variant="outline-secondary"
                      size="sm"
                      onClick={() => openItemsModal(run.runId || run.id)}
                    >
                      <i className="fas fa-list me-1"></i> View Items
                    </Button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    );
  };

  const renderItemsModal = () => (
    <Modal show={!!itemsRunId} onHide={() => setItemsRunId(null)} size="xl" centered>
      <Modal.Header closeButton>
        <Modal.Title>Payroll Items</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {itemsLoading ? (
          <div className="skeleton-table">
            <SkeletonLoader height="40px" className="mb-2" />
            {[...Array(6)].map((_, i) => (
              <SkeletonLoader key={i} height="56px" className="mb-2" />
            ))}
          </div>
        ) : (
          <div className="table-responsive">
            <Table hover className="items-table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Monthly Base Pay</th>
                  <th>Bonus</th>
                  <th>Deductions</th>
                  <th>Net Salary</th>
                </tr>
              </thead>
              <tbody>
                {runItems.map((it) => {
                  const employeeName = it.employeeName || it.employee?.fullName || it.employee?.name || 'Employee';
                  const monthlyBase = resolveMonthlyBasePay(it);
                  const bonus = resolveBonus(it);
                  const deductions = resolveDeductions(it);
                  const net = resolveNet(it);
                  return (
                    <tr key={it.itemId || `${it.employeeId}-${monthlyBase}-${net}`}>
                      <td>{employeeName}</td>
                      <td>{formatCurrency(monthlyBase)}</td>
                      <td>{formatCurrency(bonus)}</td>
                      <td>{formatCurrency(deductions)}</td>
                      <td className="fw-semibold">{formatCurrency(net)}</td>
                    </tr>
                  );
                })}
                {runItems.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-center py-4 text-muted">No items found for this run.</td>
                  </tr>
                )}
              </tbody>
            </Table>
          </div>
        )}
      </Modal.Body>
    </Modal>
  );

  return (
    <div className="payroll-management-page">
      <Container fluid>
        <div className="page-header">
          <div className="d-flex justify-content-between align-items-center mb-4 header-flex-container">
            <div className="flex-grow-1">
              <h1 className="page-title">Payroll Management</h1>
              <p className="page-subtitle text-muted">Create, process, review, and lock monthly payroll</p>
            </div>
            <div className="flex-shrink-0">
              <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                <i className="fas fa-plus me-2"></i>
                Create New Payroll Run
              </Button>
            </div>
          </div>
        </div>

        <Card className="runs-card">
          <Card.Body>
            {renderRunsTable()}
          </Card.Body>
        </Card>

        {renderCreateRunModal()}
        {renderItemsModal()}

        <ConfirmationModal
          show={!!lockingRun}
          onHide={() => !lockingInProgress && setLockingRun(null)}
          onConfirm={handleLock}
          title="Lock Payroll Run"
          message="Locking will finalize this payroll run. You won't be able to re-process it. Continue?"
          confirmText="Lock"
          confirmVariant="danger"
          variant="danger"
          isLoading={lockingInProgress}
        />
      </Container>
    </div>
  );
};

export default PayrollManagementPage;
