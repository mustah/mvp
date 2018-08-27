import {makeUrl} from '../../../helpers/urlFactory';
import {EndPoints} from '../../../services/endPoints';
import {restClient} from '../../../services/restClient';
import {EncodedUriParameters, IdNamed, toIdNamed, uuid} from '../../../types/Types';
import {SelectionListItem} from '../../user-selection/userSelectionModels';
import {Address, City} from '../location/locationModels';

export interface PagedResponse {
  items: SelectionListItem[];
  totalElements: number;
  query?: string;
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

const queryParameter = (query?: string): string => query ? `&q=${query}` : ``;

const requestParameters = (sort: string, page: number, query?: string): string =>
  `sort=${sort},asc&page=${page}${queryParameter(query)}`;

export const fetchCities = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<CityResponse, City>(
    EndPoints.cities,
    toCity,
    requestParameters('city', page, query),
  );

export const fetchAddresses = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<AddressResponse, Address>(
    EndPoints.addresses,
    toAddress,
    requestParameters('streetAddress', page, query),
  );

export const fetchFacilities = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(
    EndPoints.facilities,
    identity,
    requestParameters('externalId', page, query),
  );

export const fetchSecondaryAddresses =
  async (page: number, query?: string): Promise<PagedResponse> =>
    fetchItems<IdNamed, IdNamed>(
      EndPoints.secondaryAddresses,
      identity,
      requestParameters('address', page, query),
    );

export const fetchGatewaySerials = async (page: number, query?: string): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(
    EndPoints.gatewaySerials,
    identity,
    requestParameters('serial', page, query),
  );

export const fetchGatewayStatuses = async (): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(EndPoints.gatewayStatuses, identity);

export const fetchMeterStatuses = async (): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(EndPoints.meterStatuses, identity);

export const fetchMedia = async (): Promise<PagedResponse> =>
  fetchItems<IdNamed, IdNamed>(EndPoints.media, identity);
