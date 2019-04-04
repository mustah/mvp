import {combineReducers} from 'redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {toggle} from '../../helpers/collections';
import {resetReducer} from '../../reducers/resetReducer';
import {Medium} from '../ui/graph/measurement/measurementModels';
import {Action, uuid} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
import {
  addLegendItems,
  removeAllByType,
  ReportSector,
  showHideAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByType,
  toggleShowAverage as toggleShowAverageAction,
} from './reportActions';
import {
  LegendItem,
  LegendType,
  LegendViewOptions,
  QuantityId,
  QuantityLegendType,
  ReportState,
  SavedReportsState,
  ViewOptions
} from './reportModels';
import {getLegendItems, getLegendViewOptions, getViewOptions} from './reportSelectors';
import {initialState as initialTemporalState, temporalReducerFor} from './temporalReducer';

export const makeInitialLegendViewOptions = (): LegendViewOptions =>
  Object.keys(Medium).map(k => Medium[k])
    .reduce((acc, medium) => ({...acc, [medium]: {quantities: []}}), {aggregate: {quantities: []}});

export const initialSavedReportState: SavedReportsState = {
  meterPage: {
    id: 'meterPage',
    legendItems: [],
    legendViewOptions: makeInitialLegendViewOptions(),
    shouldShowAverage: false,
  }
};

export const initialState: ReportState = {
  savedReports: initialSavedReportState,
  temporal: initialTemporalState
};

type ActionTypes =
  | Action<LegendItem[] | uuid | LegendType | QuantityLegendType | QuantityId>
  | EmptyAction<string>;

const getLegendType = (action: ActionTypes): LegendType => (action as Action<LegendType>).payload;

const toggleLegendItemsRows = (state: SavedReportsState, type: LegendType): LegendItem[] =>
  getLegendItems(state).map(it => it.type === type ? {...it, isRowExpanded: !it.isRowExpanded} : it);

const setLegendItems = (state: SavedReportsState, legendItems: LegendItem[]): SavedReportsState => ({
    ...state,
    meterPage: {
      ...state.meterPage,
      legendItems
    }
  }
);

const toggleHiddenLines = (savedReports: SavedReportsState, legendType: LegendType): SavedReportsState => {
  if (savedReports.meterPage.legendItems.filter(it => it.type === legendType).length > 0) {
    const legendViewOptions: LegendViewOptions = savedReports.meterPage.legendViewOptions;
    const viewOptions: ViewOptions = legendViewOptions[legendType];
    return ({
        ...savedReports,
        meterPage: {
          ...savedReports.meterPage,
          legendViewOptions: {
            ...legendViewOptions,
            [legendType]: {...viewOptions, isAllLinesHidden: !viewOptions.isAllLinesHidden}
          }
        }
      }
    );
  }
  return savedReports;
};

const toggleQuantityLegendType = (
  state: SavedReportsState,
  {type, quantity}: QuantityLegendType
): SavedReportsState => {
  const legendViewOptions = getLegendViewOptions(state);
  const viewOptions: ViewOptions = legendViewOptions[type];
  const quantities = toggle(quantity, viewOptions.quantities);
  const legendItems = getLegendItems(state).map(it => it.type === type ? {...it, quantities} : it);
  return {
    ...state,
    meterPage: {
      ...state.meterPage,
      legendItems,
      legendViewOptions: {
        ...legendViewOptions,
        [type]: {...viewOptions, quantities}
      }
    }
  };
};

const removeAllByLegendType = (state: SavedReportsState, type: LegendType): SavedReportsState => {
  const legendViewOptions = state.meterPage.legendViewOptions;
  const legendItems = getLegendItems(state).filter(it => it.type !== type);
  return ({
      ...state,
      meterPage: {
        ...state.meterPage,
        legendItems,
        legendViewOptions: {
          ...legendViewOptions,
          [type]: {quantities: []}
        }
      }
    }
  );
};

const toggleQuantityId = (state: SavedReportsState, {id, quantity}: QuantityId): SavedReportsState => {
  const meters = getLegendItems(state)
    .map(it => it.id === id ? {...it, quantities: toggle(quantity, it.quantities)} : it);
  return setLegendItems(state, meters);
};

const toggleShowAverage = (state: SavedReportsState): SavedReportsState =>
  ({...state, meterPage: {...state.meterPage, shouldShowAverage: !state.meterPage.shouldShowAverage}});

const toggleLegendItemVisibility = (state: SavedReportsState, id: uuid): SavedReportsState => {
  const meters = getLegendItems(state).map(it => it.id === id ? {...it, isHidden: !it.isHidden} : it);
  return setLegendItems(state, meters);
};

const showHideAll = (state: SavedReportsState, legendType: LegendType): SavedReportsState => {
  const isAllLinesHidden = getViewOptions(state, legendType).isAllLinesHidden;
  const meters: LegendItem[] = getLegendItems(state)
    .map((prev: LegendItem) => prev.type === legendType ? {...prev, isHidden: !isAllLinesHidden} : prev);
  return toggleHiddenLines(setLegendItems(state, meters), legendType);
};

const identity = (state, _, __) => state;

export const logoutReducer = <S>(
  state: S,
  {type}: EmptyAction<string>,
  initialState: S,
): S => {
  switch (type) {
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};

export const reportReducerFor =
  (
    sector: ReportSector,
    resetState = identity
  ) =>
    (state: SavedReportsState = initialSavedReportState, action: ActionTypes): SavedReportsState => {
      switch (action.type) {
        case getType(addLegendItems(sector)):
          return setLegendItems(state, (action as Action<LegendItem[]>).payload);
        case getType(toggleLine(sector)):
          return toggleLegendItemVisibility(state, (action as Action<uuid>).payload);
        case getType(showHideAllByType(sector)):
          return showHideAll(state, getLegendType(action));
        case getType(removeAllByType(sector)):
          return removeAllByLegendType(state, getLegendType(action));
        case getType(showHideLegendRows(sector)):
          return setLegendItems(state, toggleLegendItemsRows(state, getLegendType(action)));
        case getType(toggleQuantityByType(sector)):
          return toggleQuantityLegendType(state, (action as Action<QuantityLegendType>).payload);
        case getType(toggleQuantityById(sector)):
          return toggleQuantityId(state, (action as Action<QuantityId>).payload);
        case getType(toggleShowAverageAction(sector)):
          return toggleShowAverage(state);
        default:
          return resetState(state, action, initialSavedReportState);
      }
    };

export const report = combineReducers<ReportState>({
  savedReports: reportReducerFor(ReportSector.report, logoutReducer),
  temporal: temporalReducerFor(ReportSector.report)
});

export const selectionReport = combineReducers<ReportState>({
  savedReports: reportReducerFor(ReportSector.selectionReport, resetReducer),
  temporal: temporalReducerFor(ReportSector.selectionReport)
});
