import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {SideMenuItem} from '../components/SideMenuItem';

export const SideMenuContainer = props => (
  <Column className="flex-1">
    <Row>
      <SideMenuItem name="Sparade objekt" icon="star-rate"/>
    </Row>
    <Row>
      <SideMenuItem name="Sparade filter" icon="saved-filter"/>
    </Row>
    <Row>
      <SideMenuItem name="Dynamiska grupper" icon="dynamic-groups"/>
    </Row>
    <Row>
      <SideMenuItem name="Statiska grupper" icon="static-groups"/>
    </Row>
    <Row>
      <SideMenuItem name="Fullständig katalog" icon="full-catalog"/>
    </Row>
  </Column>
);
