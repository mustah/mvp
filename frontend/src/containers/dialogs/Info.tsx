import 'Info.scss';
import * as React from 'react';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Bold, Normal} from '../../components/texts/Texts';
import {superAdminComponent} from '../../helpers/hoc';
import {Children} from '../../types/Types';

interface InfoProps {
  label: string;
  value: Children;
}

export const Info = ({label, value}: InfoProps) => {
  return value ? (
    <Column className="Info">
      <Row><Normal className="Info-label">{label}</Normal></Row>
      <Row><Bold className="first-uppercase">{value}</Bold></Row>
    </Column>
  ) : null;
};

export const SuperAdminInfo = superAdminComponent<InfoProps>(Info);
