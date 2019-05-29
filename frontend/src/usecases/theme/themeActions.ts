import {createAction, createStandardAction} from 'typesafe-actions';
import {Color} from '../../app/colors';
import {makeUpdateThemeUrlOf} from '../../helpers/urlFactory';
import {GetState} from '../../reducers/rootReducer';
import {ActionsFactory, fetchIfNeeded, FetchIfNeeded, putRequest} from '../../state/api/apiActions';
import {DataFormatter} from '../../state/domain-models/domainModelsActions';
import {ErrorResponse, Sectors, uuid} from '../../types/Types';
import {getOrganisation} from '../auth/authSelectors';
import {Colors, ThemeRequestModel, ThemeState} from './themeModels';
import {initialState} from './themeReducer';

export const requestTheme = createAction(`REQUEST_THEME`);
export const successTheme = createStandardAction(`SUCCESS_THEME`)<Colors>();
export const failureTheme = createStandardAction(`FAILURE_THEME`)<ErrorResponse>();

const actionsFactory: ActionsFactory<Colors> = _ => ({
  request: requestTheme,
  success: successTheme,
  failure: failureTheme,
});

const shouldFetchTheme: FetchIfNeeded = (getState: GetState): boolean => {
  const {error, isSuccessfullyFetched, isFetching}: ThemeState = getState().theme;
  return !isSuccessfullyFetched && !error && !isFetching;
};

const colorKeys: Array<keyof Colors> = ['primary', 'secondary'];

const dataFormatter: DataFormatter<Colors> =
  (data: any[]): Colors => {
    const responseModel: Colors = data.reduce(
      (prev, curr) =>
        curr.key && curr.value && colorKeys.includes(curr.key)
          ? ({...prev, [curr.key]: curr.value})
          : prev
      , {});
    return {...initialState.color, ...responseModel};
  };

export const fetchTheme = fetchIfNeeded<Colors>(
  Sectors.theme,
  shouldFetchTheme,
  actionsFactory,
  dataFormatter
);

export const updateTheme = putRequest<Colors, ThemeRequestModel[]>(
  Sectors.theme,
  actionsFactory,
  dataFormatter
);

export const makeBody = (primary: Color, secondary: Color): ThemeRequestModel[] =>
  [{key: 'primary', value: primary}, {key: 'secondary', value: secondary}];

export const resetColors = () =>
  async (dispatch, getState: GetState) => {
    const {id} = getOrganisation(getState().auth);
    const {color: {primary, secondary}} = initialState;
    await dispatch(updateTheme(makeUpdateThemeUrlOf(id), makeBody(primary, secondary)));
  };

export const changePrimaryColor = (color: Color, organisationId: uuid) =>
  async (dispatch, getState: GetState) => {
    const {theme: {color: {secondary}}} = getState();
    await dispatch(updateTheme(makeUpdateThemeUrlOf(organisationId), makeBody(color, secondary)));
  };

export const changeSecondaryColor = (color: Color, organisationId: uuid) =>
  async (dispatch, getState: GetState) => {
    const {theme: {color: {primary}}} = getState();
    await dispatch(updateTheme(makeUpdateThemeUrlOf(organisationId), makeBody(primary, color)));
  };
