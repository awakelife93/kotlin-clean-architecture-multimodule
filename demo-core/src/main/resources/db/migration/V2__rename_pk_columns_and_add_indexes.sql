ALTER TABLE "post" DROP CONSTRAINT IF EXISTS fk_user;

ALTER TABLE "post" ADD COLUMN deleted_dt TIMESTAMP(6);

ALTER TABLE "user" RENAME COLUMN user_id TO id;
ALTER TABLE "post" RENAME COLUMN post_id TO id;

CREATE INDEX IF NOT EXISTS idx_user_deleted_dt ON "user"(deleted_dt);
CREATE INDEX IF NOT EXISTS idx_user_role_deleted ON "user"(deleted_dt, role);

CREATE INDEX IF NOT EXISTS idx_post_user_deleted ON "post"(deleted_dt, user_id);
