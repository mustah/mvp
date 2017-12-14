import {createPayloadAction} from 'react-redux-typescript';
import {UseCases} from '../../../types/Types';
import {TabName, TabSelection} from './tabsModels';

export const CHANGE_TAB = 'CHANGE_TAB';

const changeTab = createPayloadAction<string, TabSelection>(CHANGE_TAB);

export const changeTabCollection = (tab: TabName) => changeTab({
  useCase: UseCases.collection,
  tab,
});

export const changeTabValidation = (tab: TabName) => changeTab({
  useCase: UseCases.validation,
  tab,
});
