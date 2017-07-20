import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {LinkItem} from '../components/LinkItem';

export const SideMenuContainer = props => (
  <Column className="flex-1">
    <Row>
      <LinkItem name="Sparade objekt" icon="star-rate"/>
    </Row>
    <Row>
      <LinkItem name="Sparade filter" icon="saved-filter"/>
    </Row>
    <Row>
      <LinkItem name="Dynamiska grupper" icon="dynamic-groups"/>
    </Row>
    <Row>
      <LinkItem name="Statiska grupper" icon="static-groups"/>
    </Row>
    <Row>
      <LinkItem name="Fullständig katalog" icon="full-catalog"/>
    </Row>
  </Column>
);
