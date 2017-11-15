import * as React from 'react';
import {IdNamed, statusFor} from '../../../../types/Types';
import {Row} from '../layouts/row/Row';
import {IconStatus} from '../icons/IconStatus';

export const Status = (props: IdNamed) => {
  if (!statusFor(props.id)) {
    return null;
  }
  return (
    <Row>
      <IconStatus {...props}/>
    </Row>
  );
};
