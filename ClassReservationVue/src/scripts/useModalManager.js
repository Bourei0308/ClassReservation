// scripts/useModalManager.js
import { ref, reactive } from 'vue';

// ------------------------
// Alert 弹窗状态
// ------------------------
const alertShow = ref(false);
const alertIsSuccess = ref(false);
const alertMessage = ref('');
const showAlert = (message, isSuccess) => {
  alertMessage.value = message;
  alertIsSuccess.value = isSuccess;
  alertShow.value = true;
};
const closeAlert = () => {
  alertShow.value = false;
};
const alertProps = reactive({
  get show() {
    return alertShow.value;
  },
  get isSuccess() {
    return alertIsSuccess.value;
  },
  get message() {
    return alertMessage.value;
  },
});

// ------------------------
// Confirm 弹窗状态
// ------------------------
const confirmShow = ref(false);
const confirmMessage = ref('');
let confirmCallback = null;
const openConfirm = (msg, onConfirm) => {
  confirmMessage.value = msg;
  confirmCallback = onConfirm;
  confirmShow.value = true;
};
const onConfirm = () => {
  confirmShow.value = false;
  if (confirmCallback) confirmCallback();
};
const onCancel = () => {
  confirmShow.value = false;
};

// ------------------------
// Loading 弹窗状态
// ------------------------
const loadingShow = ref(false);
const showLoading = () => {
  loadingShow.value = true;
};
const closeLoading = () => {
  loadingShow.value = false;
};

// 暴露统一接口
export function useModalManager() {
  return {
    // Alert 用
    showAlert,
    closeAlert,
    alertProps,

    // Confirm 用
    confirmShow,
    confirmMessage,
    openConfirm,
    onConfirm,
    onCancel,

    // Loading 用
    loadingShow,
    showLoading,
    closeLoading,
  };
}
