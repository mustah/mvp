import {combineReducers} from 'redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action, uuid} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {
  addLegendItems,
  removeAllByType,
  selectResolution,
  setReportTimePeriod,
  showHideAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByType,
} from './reportActions';
import {
  LegendItem,
  MediumViewOptions,
  QuantityId,
  QuantityLegendType,
  ReportState,
  SavedReportsState,
  TemporalReportState,
  ViewOptions
} from './reportModels';
import {getLegendItems, getMediumViewOptions, getViewOptions} from './reportSelectors';

export const makeInitialLegendTypeViewOptions = (): MediumViewOptions =>
  Object.keys(Medium).map(k => Medium[k])
    .reduce((acc, medium) => ({...acc, [medium]: {quantities: []}}), {aggregate: {quantities: []}});

export const mediumViewOptions: MediumViewOptions = makeInitialLegendTypeViewOptions();

export const initialSavedReportState: SavedReportsState = {
  meterPage: {
    id: 'meterPage',
    legendItems: [],
    mediumViewOptions,
  }
};

export const initialTemporalState: TemporalReportState = {
  resolution: TemporalResolution.hour,
  timePeriod: {period: Period.latest},
};

export const initialState: ReportState = {
  savedReports: initialSavedReportState,
  temporal: initialTemporalState
};

const getMedium = (action: ActionTypes): Medium => (action as Action<Medium>).payload;

const toggleLegendItemsRows = (state: SavedReportsState, medium: Medium): LegendItem[] =>
  getLegendItems(state).map(it => it.type === medium ? {...it, isRowExpanded: !it.isRowExpanded} : it);

const getLegendItemsNotMatchingMedium = (state: SavedReportsState, medium: Medium): LegendItem[] =>
  getLegendItems(state).filter(it => it.type !== medium);

const getLegendItemsMatchingMedium = (state: SavedReportsState, medium: Medium): LegendItem[] =>
  getLegendItems(state).filter(it => it.type === medium);

const makeSavedReports = (state: SavedReportsState, legendItems: LegendItem[]): SavedReportsState => ({
    ...state,
    ['meterPage']: {
      ...state.meterPage,
      legendItems
    }
  }
);

const toggleHiddenLines = (savedReports: SavedReportsState, medium: Medium): SavedReportsState => {
  if (savedReports.meterPage.legendItems.filter(it => it.type === medium).length > 0) {
    const mediumViewOptions: MediumViewOptions = savedReports.meterPage.mediumViewOptions;
    const viewOptions: ViewOptions = mediumViewOptions[medium];
    return ({
        ...savedReports,
        ['meterPage']: {
          ...savedReports.meterPage,
          mediumViewOptions: {
            ...mediumViewOptions,
            [medium]: {...viewOptions, isAllLinesHidden: !viewOptions.isAllLinesHidden}
          }
        }
      }
    );
  }
  return savedReports;
};

const toggleQuantityMedium = (state: SavedReportsState, {type, quantity}: QuantityLegendType): SavedReportsState => {
  const mediumViewOptions = getMediumViewOptions(state);
  const viewOptions: ViewOptions = mediumViewOptions[type];
  const quantities = toggle(quantity, viewOptions.quantities);
  const legendItems = getLegendItems(state).map(it => it.type === type ? {...it, quantities} : it);
  return {
    ...state,
    ['meterPage']: {
      ...state.meterPage,
      legendItems,
      mediumViewOptions: {
        ...mediumViewOptions,
        [type]: {...viewOptions, quantities}
      }
    }
  };
};

const toggleQuantityId = (state: SavedReportsState, {id, quantity}: QuantityId): SavedReportsState => {
  const meters = getLegendItems(state)
    .map(it => it.id === id ? {...it, quantities: toggle(quantity, it.quantities)} : it);
  return makeSavedReports(state, meters);
};

const toggleLegendItemVisibility = (state: SavedReportsState, id: uuid): SavedReportsState => {
  const meters = getLegendItems(state).map(it => it.id === id ? {...it, isHidden: !it.isHidden} : it);
  return makeSavedReports(state, meters);
};

const showHideAll = (state: SavedReportsState, medium: Medium): SavedReportsState => {
  const isAllLinesHidden = getViewOptions(state, medium).isAllLinesHidden;
  const meters: LegendItem[] = getLegendItemsMatchingMedium(state, medium)
    .map(it => ({...it, isHidden: !isAllLinesHidden}));
  return toggleHiddenLines(makeSavedReports(state, meters), medium);
};

type ActionTypes = Action<TemporalResolution | SelectionInterval> | EmptyAction<string>;

export const temporal =
  (state: TemporalReportState = initialTemporalState, action: ActionTypes): TemporalReportState => {
    switch (action.type) {
      case getType(setReportTimePeriod):
        return {...state, timePeriod: {...(action as Action<SelectionInterval>).payload}};
      case getType(selectResolution):
        return {...state, resolution: (action as Action<TemporalResolution>).payload};
      case getType(logoutUser):
        return initialTemporalState;
      default:
        return state;
    }
  };

type SavedReportsActionTypes =
  | Action<LegendItem[] | uuid | Medium | QuantityLegendType | QuantityId>
  | EmptyAction<string>;

export const savedReports =
  (state: SavedReportsState = initialSavedReportState, action: SavedReportsActionTypes): SavedReportsState => {
    switch (action.type) {
      case getType(addLegendItems):
        return makeSavedReports(state, (action as Action<LegendItem[]>).payload);
      case getType(toggleLine):
        return toggleLegendItemVisibility(state, (action as Action<uuid>).payload);
      case getType(showHideAllByType):
        return showHideAll(state, getMedium(action));
      case getType(removeAllByType):
        return makeSavedReports(state, getLegendItemsNotMatchingMedium(state, getMedium(action)));
      case getType(showHideLegendRows):
        return makeSavedReports(state, toggleLegendItemsRows(state, getMedium(action)));
      case getType(toggleQuantityByType):
        return toggleQuantityMedium(state, (action as Action<QuantityLegendType>).payload);
      case getType(toggleQuantityById):
        return toggleQuantityId(state, (action as Action<QuantityId>).payload);
      case getType(logoutUser):
        return initialSavedReportState;
      default:
        return state;
    }
  };

export const report = combineReducers<ReportState>({
  savedReports,
  temporal
});
