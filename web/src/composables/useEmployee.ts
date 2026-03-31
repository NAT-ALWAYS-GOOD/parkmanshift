import { ref } from 'vue'

const STORAGE_KEY = 'parkmanshift_employee_id'

const employeeId = ref<string>(localStorage.getItem(STORAGE_KEY) ?? '')

export function useEmployee() {
  function setEmployeeId(id: string) {
    employeeId.value = id
    localStorage.setItem(STORAGE_KEY, id)
  }

  return { employeeId, setEmployeeId }
}