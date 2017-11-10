import {createPayloadAction} from 'react-redux-typescript';
import {useCases} from '../../../types/constants';
import {tabType} from '../../../usecases/common/components/tabs/models/TabsModel';

export const TABS_CHANGE_TAB = 'TABS_CHANGE_TAB';
export const TABS_CHANGE_TAB_OPTION = 'TABS_CHANGE_TAB_OPTION';

const changeTab = createPayloadAction(TABS_CHANGE_TAB);
const changeTabOption = createPayloadAction(TABS_CHANGE_TAB_OPTION);

export const changeTabCollection = (tab: tabType) => changeTab({
  useCase: useCases.collection,
  tab,
});

export const changeTabOptionCollection = (tab: tabType, option: string) => changeTabOption({
  useCase: useCases.collection,
  tab,
  option,
});

export const changeTabValidation = (tab: tabType) => changeTab({
  useCase: useCases.validation,
  tab,
});

export const changeTabOptionValidation = (tab: tabType, option: string) => changeTabOption({
  useCase: useCases.validation,
  tab,
  option,
});
