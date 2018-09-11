import * as React from 'react';
import {statusTranslation} from '../../helpers/translations';
import {statusFor} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';

interface Props {
  label: string;
}

export const Status = ({label}: Props) => (
  <Row>
    <IconStatus status={statusFor(label)} label={statusTranslation(label)}/>
  </Row>
);
