import * as React from 'react';
import {Row} from '../../../layouts/components/row/Row';
import {Xlarge} from './Texts';
import './Title.scss';

interface TitleProps {
  children: React.ReactNode[] | string;
}

export const Title = (props: TitleProps) => (
  <Row className="Title">
    <Xlarge className="Bold">{props.children}</Xlarge>
  </Row>
);
