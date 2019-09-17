import {createStandardAction} from 'typesafe-actions';
import {BatchReferencePayload} from '../../../../state/domain-models-paginated/batch-references/batchReferenceModels';

export const changeBatchReference = createStandardAction('CREATE_BATCH_REFERENCE')<BatchReferencePayload>();

export const changeRequireApproval = createStandardAction('CHANGE_REQUIRE_APPROVAL')<boolean>();

export const addDeviceEuis = createStandardAction('ADD_DEVICE_EUIS')<string[]>();

export const changeDeviceEuis = createStandardAction('CHANGE_DEVICE_EUIS')<string>();
