import {makeUrl} from '../../../helpers/urlFactory';
import {EndPoints} from '../../../services/endPoints';
import {restClient} from '../../../services/restClient';
import {translate} from '../../../services/translationService';
import {EncodedUriParameters, IdNamed, Status, toIdNamed, uuid} from '../../../types/Types';
import {Query} from '../../search/searchModels';
import {SelectionListItem} from '../../user-selection/userSelectionModels';
import {Address, City} from '../location/locationModels';

export interface PagedResponse extends Query {
  items: SelectionListItem[];
  totalElements: number;
}

export type FetchByPage = (page: number, query?: string) => Promise<PagedResponse>;

interface CityResponse {
  name: string;
  country: string;
}

interface AddressResponse {
  street: string;
  city: string;
  country: string;
}

const identity = <T>(id: T): T => id;

const asUnselected = (item: IdNamed): SelectionListItem => ({...item, selected: false});

const translateIdNamed = (item: IdNamed): IdNamed => ({...item, name: translate(item.name)});

const toCity = ({name, country}: CityResponse): City => ({
  id: `${country},${name}`,
  name,
  country: toIdNamed(country),
});

const toAddress = ({street, city, country}: AddressResponse) => ({
  id: `${country},${city},${street}`,
  name: street,
  city: toIdNamed(city),
  country: toIdNamed(country),
});

const getIdParts = (id: uuid = ''): string[] => id.toString().split(',');

export const mapSelectedIdToCity = (id: uuid): City => {
  const idParts = getIdParts(id);
  return {
    id,
    name: idParts[1],
    country: {...toIdNamed(idParts[0])},
  };
};

export const mapSelectedIdToAddress = (id: uuid): Address => {
  const idParts = getIdParts(id);
  return {
    id,
    name: idParts[2],
    city: {...toIdNamed(idParts[1])},
    country: {...toIdNamed(idParts[0])},
  };
};

const fetchItems = async <T, R>(
  endpoint: EndPoints,
  contentMapper: (value: T) => R,
  parameters?: EncodedUriParameters,
): Promise<PagedResponse> => {
  const {data: {content, totalElements}} = await restClient.get(makeUrl(endpoint, parameters));
  return {items: content.map(contentMapper), totalElements};
};

const loadStaticItems = (items: SelectionListItem[]): Promise<PagedResponse> =>
  new Promise<PagedResponse>((resolve) => resolve({items, totalElements: items.length}));

const makeRequestParameters = (...parameters: string[]): string => parameters
  .filter((param) => param.length > 0).join('&');

const queryParameter = (query?: string): string => query ? `q=${query}` : ``;

const sortParameter = (sort?: string): string => sort ? `sort=${sort},asc` : ``;

const pageParameter = (page: number): string => `page=${page}`;

const requestParameters = (page: number, sort: string, query?: string): string =>
  makeRequestParameters(
    sortParameter(query ? undefined : sort),
    pageParameter(page),
    queryParameter(query)
  );

export const fetchCities = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<CityResponse, City>(
    EndPoints.cities,
    toCity,
    requestParameters(page, 'city', query),
  );

export const fetchAddresses = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<AddressResponse, Address>(
    EndPoints.addresses,
    toAddress,
    requestParameters(page, 'streetAddress', query),
  );

export const fetchFacilities = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(
    EndPoints.facilities,
    identity,
    requestParameters(page, 'externalId', query),
  );

export const fetchOrganisationsToSelect = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(
    EndPoints.organisationsToSelect,
    identity,
    requestParameters(page, 'name', query),
  );

export const fetchSecondaryAddresses =
  async (page: number, query?: string): Promise<PagedResponse> =>
    fetchItems<IdNamed, IdNamed>(
      EndPoints.secondaryAddresses,
      identity,
      requestParameters(page, 'secondaryAddress', query),
    );

export const fetchGatewaySerials = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(
    EndPoints.gatewaySerials,
    identity,
    requestParameters(page, 'serial', query),
  );

const yesNoReported: IdNamed[] = [{id: Status.error, name: 'yes'}, {id: Status.ok, name: 'no'}];

export const fetchReported = async (): Promise<PagedResponse> =>
  loadStaticItems(yesNoReported.map(translateIdNamed).map(asUnselected));

const yesNoAlarms: IdNamed[] = [toIdNamed('yes'), toIdNamed('no')];

export const fetchAlarms = (): Promise<PagedResponse> =>
  loadStaticItems(yesNoAlarms.map(translateIdNamed).map(asUnselected));

export const fetchMedia = async (): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(EndPoints.media, identity);
