import * as React from 'react';
import {Row} from '../../../components/layouts/row/Row';
import {Normal} from '../../../components/texts/Texts';
import './EmptyContentText.scss';

interface Props {
  text: string;
}

export const EmptyContentText = ({text}: Props) => (
  <Row className="EmptyContentText">
    <Normal className="Italic first-uppercase">{text}</Normal>
  </Row>
);
