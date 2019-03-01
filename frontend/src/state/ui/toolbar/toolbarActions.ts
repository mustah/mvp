import {createStandardAction} from 'typesafe-actions';
import {payloadActionOf} from '../../../types/Types';
import {ToolbarView} from './toolbarModels';

export const CHANGE_TOOLBAR_VIEW = 'CHANGE_TOOLBAR_VIEW';

export const changeToolbarView = payloadActionOf<ToolbarView>(CHANGE_TOOLBAR_VIEW);
export const changeCollectionToolbarView = createStandardAction('CHANGE_COLLECTION_TOOLBAR_VIEW')<string>();
