import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {LinkItem} from '../components/LinkItem';

export const SideMenuContainer = props => (
  <Column className="flex-1">
    <LinkItem name="Sparade objekt" icon="star"/>
    <LinkItem name="Sparade filter" icon="folder-star"/>
    <LinkItem name="Dynamiska grupper" icon="autorenew"/>
    <LinkItem name="Statiska grupper" icon="folder-star"/>
    <LinkItem name="Fullständig katalog" icon="folder-star"/>
  </Column>
);
