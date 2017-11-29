import {createPayloadAction} from 'react-redux-typescript';
import {useCases} from '../../../types/constants';
import {TabSelection, TopLevelTab} from './tabsModels';

export const TABS_CHANGE_TAB = 'TABS_CHANGE_TAB';
export const TABS_CHANGE_TAB_OPTION = 'TABS_CHANGE_TAB_OPTION';

const changeTab = createPayloadAction<string, TabSelection>(TABS_CHANGE_TAB);
const changeTabOption = createPayloadAction<string, TabSelection>(TABS_CHANGE_TAB_OPTION);

export const changeTabCollection = (tab: TopLevelTab) => changeTab({
  useCase: useCases.collection,
  tab,
});

export const changeTabOptionCollection = (tab: TopLevelTab, option: string) => changeTabOption({
  useCase: useCases.collection,
  tab,
  option,
});

export const changeTabValidation = (tab: TopLevelTab) => changeTab({
  useCase: useCases.validation,
  tab,
});

export const changeTabOptionValidation = (tab: TopLevelTab, option: string) => changeTabOption({
  useCase: useCases.validation,
  tab,
  option,
});
