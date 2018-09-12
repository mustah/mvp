import * as classNames from 'classnames';
import 'Info.scss';
import * as React from 'react';
import {componentOrNull} from '../../components/hoc/hocs';
import {superAdminOnly} from '../../components/hoc/withRoles';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Bold, Normal} from '../../components/texts/Texts';
import {Children, ClassNamed} from '../../types/Types';

interface InfoProps extends ClassNamed {
  label: string;
  value: Children;
}

const InfoComponent = ({className, label, value}: InfoProps) => (
  <Column className={classNames('Info', className)}>
    <Row><Normal className="Info-label">{label}</Normal></Row>
    <Row><Bold className="first-uppercase">{value}</Bold></Row>
  </Column>
);

export const Info = componentOrNull<InfoProps>(({value}: InfoProps) => !!value)(InfoComponent);

export const SuperAdminInfo = superAdminOnly<InfoProps>(Info);
