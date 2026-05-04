import { BackupDatePipe } from './backup-date.pipe';

describe('BackupDatePipe', () => {
  const pipe = new BackupDatePipe();

  it('formats last backup date for admin status', () => {
    const formatted = pipe.transform('2026-05-04T12:30:00Z');

    expect(formatted).toContain('04/05/2026');
    expect(formatted).toContain('09:30');
  });

  it('shows missing backup explicitly', () => {
    expect(pipe.transform(null)).toBe('Sem backup');
  });
});
