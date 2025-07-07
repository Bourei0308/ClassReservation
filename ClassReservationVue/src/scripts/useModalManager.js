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
let cancelCallback = null;

const openConfirm = (msg, onConfirm) => {
  confirmMessage.value = msg;
  confirmCallback = onConfirm;
  cancelCallback = null; // 手动打开时清除 cancelCallback
  confirmShow.value = true;
};

// ✅ 新增支持 async 的 confirm
const openConfirmAsync = (msg) => {
  return new Promise((resolve) => {
    confirmMessage.value = msg;
    confirmShow.value = true;

    confirmCallback = () => {
      confirmShow.value = false;
      resolve(true);
    };

    cancelCallback = () => {
      confirmShow.value = false;
      resolve(false);
    };
  });
};

const onConfirm = () => {
  confirmShow.value = false;
  if (confirmCallback) {
    confirmCallback();
    confirmCallback = null;
  }
};

const onCancel = () => {
  confirmShow.value = false;
  if (cancelCallback) {
    cancelCallback();
    cancelCallback = null;
  }
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

// ✅ 暴露统一接口
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
    openConfirmAsync,
    onConfirm,
    onCancel,

    // Loading 用
    loadingShow,
    showLoading,
    closeLoading,
  };
}
