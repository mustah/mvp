import {createPayloadAction} from 'react-redux-typescript';
import {tabType} from '../../../usecases/common/components/tabs/models/TabsModel';

export const TABS_CHANGE_TAB = 'TABS_CHANGE_TAB';
export const TABS_CHANGE_TAB_OPTION = 'TABS_CHANGE_TAB_OPTION';

const changeTab = createPayloadAction(TABS_CHANGE_TAB);
const changeTabOption = createPayloadAction(TABS_CHANGE_TAB_OPTION);

const DASHBOARD = 'dashboard';
const COLLECTION = 'collection';
const VALIDATION = 'validation';

export const changeTabDashboard = (tab: tabType) => changeTab({
  useCase: DASHBOARD,
  tab,
});
export const changeTabOptionDashboard = (tab: tabType, option: string) => changeTabOption({
  useCase: DASHBOARD,
  tab,
  option,
});

export const changeTabCollection = (tab: tabType) => changeTab({
  useCase: COLLECTION,
  tab,
});
export const changeTabOptionCollection = (tab: tabType, option: string) => changeTabOption({
  useCase: COLLECTION,
  tab,
  option,
});

export const changeTabValidation = (tab: tabType) => changeTab({
  useCase: VALIDATION,
  tab,
});
export const changeTabOptionValidation = (tab: tabType, option: string) => changeTabOption({
  useCase: VALIDATION,
  tab,
  option,
});
