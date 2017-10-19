import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {Xlarge} from './Texts';
import './Title.scss';

interface TitleProps {
  children: React.ReactNode[] | React.ReactNode;
}

export const Title = (props: TitleProps) => (
  <Row className="Title">
    <Xlarge className="Bold">{props.children}</Xlarge>
  </Row>
);

export const MainTitle = (props: TitleProps) => (
  <Row className="MainTitle">
    <Xlarge className="Bold">{props.children}</Xlarge>
  </Row>
);
