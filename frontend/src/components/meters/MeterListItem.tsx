import * as React from 'react';
import {routes} from '../../app/routes';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ButtonInfo} from '../buttons/ButtonInfo';
import {Row} from '../layouts/row/Row';
import {Link} from '../links/Link';

interface Props {
  meter: Meter;
  subPath?: string;
}

export const MeterListItem = ({meter: {facility, id}, subPath = ''}: Props) => (
  <Row>
    <Link to={`${routes.meter}/${id}${subPath}`}>
      <ButtonInfo label={facility} title={facility.toString()}/>
    </Link>
  </Row>
);
