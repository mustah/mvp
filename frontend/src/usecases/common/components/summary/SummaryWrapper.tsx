import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {Summary} from './Summary';
import './Summary.scss';

export const SummaryWrapper = () => {
  return (
    <RowCenter className="SummaryWrapper">
      <Summary title="Städer" count="10"/>
      <Summary title="Adresser" count="22"/>
      <Summary title="Mätpunkter" count="2321"/>
    </RowCenter>
  );
};
