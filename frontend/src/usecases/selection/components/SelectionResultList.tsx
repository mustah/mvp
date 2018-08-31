import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import './SelectionResultList.scss';

export const SearchResultList = () => (
  <Column className="SearchResultList">
    <MeterListContainer componentId="selectionMeterList"/>
  </Column>
);
