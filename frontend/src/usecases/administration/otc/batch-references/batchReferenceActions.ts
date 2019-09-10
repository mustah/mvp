import {createStandardAction} from 'typesafe-actions';

export const changeBatchReference = createStandardAction('CREATE_BATCH_REFERENCE')<string>();

export const changeRequireApproval = createStandardAction('CHANGE_REQUIRE_APPROVAL')<boolean>();

export const changeDeviceEuis = createStandardAction('CHANGE_DEVICE_EUIS')<string>();
