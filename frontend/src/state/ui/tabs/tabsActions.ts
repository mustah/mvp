import {createPayloadAction} from 'react-redux-typescript';
import {UseCases} from '../../../types/Types';
import {TabName, TabSelection} from './tabsModels';

export const CHANGE_TAB = 'CHANGE_TAB';

const changeTab = createPayloadAction<string, TabSelection>(CHANGE_TAB);

export const changeTabGateway = (tab: TabName) => changeTab({
  useCase: UseCases.collection,
  tab,
});

export const changeTabMeter = (tab: TabName) => changeTab({
  useCase: UseCases.validation,
  tab,
});

export const changeTabReport = (tab: TabName) => changeTab({
  useCase: UseCases.report,
  tab,
});
