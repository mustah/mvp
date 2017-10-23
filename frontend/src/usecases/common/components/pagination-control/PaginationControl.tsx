import * as React from 'react';
import {Pagination} from '../../../collection/models/Collections';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';

interface PaginationControlProps {
  pagination: Pagination;
  changePage: (page) => any;
}

export const PaginationControl = (props: PaginationControlProps) => {
  const {pagination: {page, total, limit}, changePage} = props;
  const pages = Math.floor(total / limit);
  const changePageNext = () => changePage(page + 1);
  const changePagePrev = () => changePage(page - 1);
  const prevPage = page === 1 ? null : (
    <div onClick={changePagePrev}>
      Previous page
    </div>
  );
  const nextPage = (page >= pages) ? null : (
    <div onClick={changePageNext}>
      Next page
    </div>
  );
  return (
    <Row>
      {prevPage}
      <div>
        <Bold>{page} / {pages}</Bold>
      </div>
      {nextPage}
    </Row>
  );
};
