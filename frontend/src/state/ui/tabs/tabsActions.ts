import {createAction, createStandardAction} from 'typesafe-actions';
import {UseCases} from '../../../types/Types';
import {TabName, TabSelection} from './tabsModels';

export const unknownAction = createAction('unknown');

export const changeTab = createStandardAction('CHANGE_TAB')<TabSelection>();

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
