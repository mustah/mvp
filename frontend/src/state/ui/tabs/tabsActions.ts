import {createPayloadAction} from 'react-redux-typescript';
import {useCases} from '../../../types/constants';
import {TabName, TabSelection} from './tabsModels';

export const TABS_CHANGE_TAB = 'TABS_CHANGE_TAB';

const changeTab = createPayloadAction<string, TabSelection>(TABS_CHANGE_TAB);

export const changeTabCollection = (tab: TabName) => changeTab({
  useCase: useCases.collection,
  tab,
});

export const changeTabValidation = (tab: TabName) => changeTab({
  useCase: useCases.validation,
  tab,
});
